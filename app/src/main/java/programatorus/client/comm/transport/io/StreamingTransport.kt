package programatorus.client.comm.transport.io

import android.util.Log
import programatorus.client.comm.AbstractConnection
import programatorus.client.comm.transport.*
import programatorus.client.comm.transport.wrapper.OutgoingPacket
import java.io.*
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


abstract class StreamingTransport<T : StreamingTransport<T>>(
    private val mClient: ITransportClient,
) : AbstractConnection(mClient), ITransport {

    companion object {
        private const val TAG = "StreamingTransport"
        private const val MAX_SIZE = 1024
        private const val PREAMBLE = 0b1010_1010
    }

    private var mOutputQueue: BlockingQueue<OutgoingPacket> = LinkedBlockingQueue()
    private var mRunning: AtomicBoolean = AtomicBoolean(false)

    private var mInputThread: IOThread<T>? = null

    private var mOutputThread: IOThread<T>? = null

    protected abstract val inputStream: InputStream?

    protected abstract val outputStream: OutputStream?

    protected abstract val isConnected: Boolean

    protected abstract fun doConnect(): Boolean

    protected abstract fun doDisconnect()

    protected open val disconnectOnReconnect: Boolean
        get() = true

    override fun send(packet: ByteArray): OutgoingPacket {
        Log.d(TAG, "send()")
        val outgoing = OutgoingPacket(packet)

        mOutputQueue.add(outgoing)

        if (state != ConnectionState.CONNECTED) {
            reconnect()
        }

        return outgoing
    }

    override fun reconnect() {
        Log.d(TAG, "reconnect()")
        if (isConnected && disconnectOnReconnect) {
            disconnect()
        }
        connect()
    }

    private fun connect() {
        Log.d(TAG, "connect()")
        if ((isConnected || inputStream != null || outputStream != null) && disconnectOnReconnect) {
            disconnect()
        }

        state = ConnectionState.CONNECTING

        if (!doConnect() || !isConnected) {
            disconnect()
            state = ConnectionState.DISCONNECTED
            return
        }

        mInputThread = IOThread("input", this::inputThreadTask)
        mInputThread?.start()

        mOutputThread = IOThread("output", this::outputThreadTask)
        mOutputThread?.start()

        state = ConnectionState.CONNECTED
    }

    /**
     * This function is ran on mInputThread, NOT ON mExecutor
     */
    private fun inputThreadTask() {
        Log.d(TAG, "inputThreadTask(): Trying to parse from input stream")

        val frameDecoder = FrameDecoder(inputStream!!)
        val buffer = frameDecoder.readFrame() ?: return
        mClient.onPacketReceived(buffer)
    }

    /**
     * This function is ran on mOutputThread
     */
    private fun outputThreadTask() {
        Log.d(TAG, "outputThreadTask(): Dequeueing from output queue")
        val outgoing = mOutputQueue.poll(Long.MAX_VALUE, TimeUnit.HOURS) ?: return

        try {
            Log.d(TAG, "outputThreadTask(): Trying to write to output stream")

            val frameEncoder = FrameEncoder(outputStream!!)
            frameEncoder.startFrame()
            frameEncoder.write(outgoing.packet)
            frameEncoder.finishFrame()

            outputStream?.flush()
            Log.d(TAG, "outputThreadTask(): Successfully sent packet size=${outgoing.packet.size}")

            outgoing.response.complete(outgoing)
        } catch (th: Throwable) {
            outgoing.response.completeExceptionally(th)
        }
    }

    override fun disconnect() {
        Log.d(TAG, "disconnect()")
        state = ConnectionState.DISCONNECTING

        mRunning.set(false)

        try {
            mInputThread?.interrupt()
            mOutputThread?.interrupt()
        } catch (_: Throwable) {
        }

        mInputThread = null
        mOutputThread = null

        doDisconnect()

        state = ConnectionState.DISCONNECTED
    }

    private inner class IOThread<T : StreamingTransport<T>>(
        name: String,
        private val mIoOperation: () -> Unit
    ) : Thread("IO Thread - $name") {

        override fun run() {
            try {
                while (isConnected) {
                    try {
                        mIoOperation()
                    } catch (th: InterruptedIOException) {
                        if (!mRunning.get())
                            return
                        Log.e(TAG, "run(): ", th)
                    } catch (th: InterruptedException) {
                        if (!mRunning.get())
                            return
                        Log.e(TAG, "run(): ", th)
                    }
                }
            } catch (th: Throwable) {
                if (!mRunning.get())
                    return
                try {
                    Log.e(TAG, "run(): IO thread dead", th)
                } catch (_: RuntimeException) {
                    th.printStackTrace()
                }
            }
        }
    }

}