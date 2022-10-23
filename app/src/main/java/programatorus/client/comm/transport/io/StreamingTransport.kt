package programatorus.client.comm.transport.io

import android.util.Log
import programatorus.client.WeakRefFactoryMixin
import programatorus.client.comm.AbstractConnection
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.comm.transport.ITransport
import programatorus.client.comm.transport.ITransportClient
import programatorus.client.comm.transport.wrapper.OutgoingPacket
import java.io.*
import java.lang.ref.WeakReference
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

abstract class StreamingTransport<T : StreamingTransport<T>>(
    client: ITransportClient,
) : AbstractConnection(client), ITransport {

    companion object {
        private const val TAG = "StreamingTransport"
        private const val MAX_SIZE = 1024
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
        if (isConnected) {
            disconnect()
        }
        connect()
    }

    private fun connect() {
        Log.d(TAG, "connect()")
        if (isConnected || inputStream != null || outputStream != null) {
            disconnect()
        }

        state = ConnectionState.CONNECTING

        if (!doConnect() || !isConnected) {
            disconnect()
            state = ConnectionState.DISCONNECTED
            return
        }

        mInputThread = IOThread(this::inputThreadTask)
        mInputThread?.start()

        mOutputThread = IOThread(this::outputThreadTask)
        mOutputThread?.start()

        state = ConnectionState.CONNECTED
    }

    /**
     * This function is ran on mInputThread, NOT ON mExecutor
     */
    private fun inputThreadTask() {
        Log.d(TAG, "Trying to parse from input stream")

        val dataInputStream = DataInputStream(inputStream!!)
        val size = dataInputStream.readInt()

        if (size > MAX_SIZE) {
            throw IOException("Too big packet requested")
        }

        val buffer = ByteArray(size)
        var pos = 0

        while (pos < size) {
            val read = dataInputStream.read(buffer, pos, size - pos)

            if (read == -1) {
                throw IOException("Unexpected EOF")
            }

            pos += read
        }
        Log.d(TAG, "Received packet size=$size")

        client.onPacketReceived(buffer)
    }

    /**
     * This function is ran on mOutputThread, NOT ON mExecutor
     */
    private fun outputThreadTask() {
        Log.d(TAG, "Dequeueing from output queue")
        val outgoing = mOutputQueue.poll(Long.MAX_VALUE, TimeUnit.HOURS) ?: return

        try {
            Log.d(TAG, "Trying to write to output stream")

            val dataOutgoingStream = DataOutputStream(outputStream!!)
            dataOutgoingStream.writeInt(outgoing.packet.size)
            dataOutgoingStream.write(outgoing.packet, 0, outgoing.packet.size)

            outputStream?.flush()

            outgoing.response.complete(outgoing)
        } catch (th: Throwable) {
            outgoing.response.completeExceptionally(th)
        }
    }

    override fun disconnect() {
        Log.d(TAG, "disconnect()")
        state = ConnectionState.DISCONNECTING

        mRunning.set(false)

        mInputThread?.interrupt()
        mOutputThread?.interrupt()

        mInputThread = null
        mOutputThread = null

        doDisconnect()

        state = ConnectionState.DISCONNECTED
    }

    private inner class IOThread<T : StreamingTransport<T>>(
        private val mIoOperation: () -> Unit
    ) : Thread() {

        override fun run() {
            try {
                while (isConnected) {
                    try {
                        mIoOperation()
                    } catch (th: InterruptedIOException) {
                        if (!isConnected)
                            return
                        Log.e(TAG, "", th)
                    } catch (th: InterruptedException) {
                        if (!isConnected)
                            return
                        Log.e(TAG, "", th)
                    }
                }
            } catch (th: Throwable) {
                Log.e(TAG, "IO thread dead", th)
            }
        }
    }

}