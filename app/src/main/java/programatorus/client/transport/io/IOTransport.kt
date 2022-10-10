package programatorus.client.transport.io

import android.os.Handler
import android.os.Looper
import android.util.Log
import programatorus.client.WeakRefFactoryMixin
import programatorus.client.transport.AbstractTransport
import programatorus.client.transport.ConnectionState
import programatorus.client.transport.ITransportClient
import programatorus.client.transport.wrapper.OutgoingPacket
import java.io.*
import java.lang.ref.WeakReference
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean

abstract class IOTransport<T : IOTransport<T>>(
    client: ITransportClient,
    private val mHandler: Handler = Handler(Looper.getMainLooper())
) : AbstractTransport(client), WeakRefFactoryMixin<T> {

    companion object {
        private const val TAG = "IOTransport"
        private const val MAX_SIZE = 1024
    }

    private var mOutputQueue: BlockingQueue<OutgoingPacket> = ArrayBlockingQueue(16)
    private var mRunning: AtomicBoolean = AtomicBoolean(false)

    private var mInputThread: IOThread<T>? = null

    private var mOutputThread: IOThread<T>? = null

    protected abstract val inputStream: InputStream?

    protected abstract val outputStream: OutputStream?

    protected abstract val isConnected: Boolean

    protected abstract fun doConnect(): Boolean

    protected abstract fun doDisconnect()

    override fun send(packet: ByteArray): OutgoingPacket {
        val outgoing = OutgoingPacket(packet)
        mHandler.post { sendTask(outgoing) }
        return outgoing
    }

    private fun sendTask(outgoing: OutgoingPacket) {
        mOutputQueue.add(outgoing)
    }

    override fun reconnect() {
        if (isConnected) {
            disconnect()
        }
        connect()
    }

    private fun connect(): Boolean {
        if (isConnected || inputStream != null || outputStream != null) {
            disconnect()
        }

        state = ConnectionState.CONNECTING

        if (!doConnect() || !isConnected) {
            disconnect()
            state = ConnectionState.DISCONNECTED
            return false
        }

        mInputThread = IOThread(weakRefFromThis(), this::inputThreadTask)
        mInputThread?.start()

        mOutputThread = IOThread(weakRefFromThis(), this::outputThreadTask)
        mOutputThread?.start()

        state = ConnectionState.CONNECTED
        return true
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

        mHandler.post { client.onPacketReceived(buffer) }
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

            mHandler.post { outgoing.response.complete(outgoing) }
        } catch (th: Throwable) {
            outgoing.response.completeExceptionally(th)
        }
    }

    override fun disconnect() {
        state = ConnectionState.DISCONNECTING

        mRunning.set(false)

        mInputThread?.interrupt()
        mOutputThread?.interrupt()

        mInputThread = null
        mOutputThread = null

        doDisconnect()

        state = ConnectionState.DISCONNECTED
    }

    class IOThread<T : IOTransport<T>>(
        private val mWeakTransport: WeakReference<T>,
        private val mIoOperation: () -> Unit
    ) : Thread() {

        private val isRunning: Boolean
            get() {
                val comm = mWeakTransport.get()
                return comm?.isConnected ?: false
            }

        override fun run() {
            try {
                while (isRunning) {
                    try {
                        mIoOperation()
                    } catch (th: InterruptedIOException) {
                        if (!isRunning)
                            return
                        Log.e(TAG, "", th)
                    } catch (th: InterruptedException) {
                        if (!isRunning)
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