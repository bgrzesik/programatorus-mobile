package programus.client.transport.io

import android.util.Log
import programus.client.WeakRefFactoryMixin
import programus.client.transport.AbstractTransport
import programus.client.transport.ConnectionState
import programus.client.transport.ITransportClient
import programus.client.transport.wrapper.OutgoingMessage
import programus.proto.GenericMessage
import java.io.InputStream
import java.io.InterruptedIOException
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean

abstract class IOTransport<T : IOTransport<T>>(
    client: ITransportClient,
    private val mExecutor: ScheduledExecutorService = ScheduledThreadPoolExecutor(1)
) : AbstractTransport(client), WeakRefFactoryMixin<T> {

    companion object {
        const val TAG = "IOTransport"
    }

    private var mOutputQueue: BlockingQueue<OutgoingMessage> = ArrayBlockingQueue(16)
    private var mRunning: AtomicBoolean = AtomicBoolean(false)

    private var mInputThread: IOThread<T>? = null

    private var mOutputThread: IOThread<T>? = null

    protected abstract val inputStream: InputStream?

    protected abstract val outputStream: OutputStream?

    protected abstract val isConnected: Boolean

    protected abstract fun doConnect(): Boolean

    protected abstract fun doDisconnect()

    override fun send(message: GenericMessage): OutgoingMessage {
        val outgoing = OutgoingMessage(message)
        mExecutor.submit { sendTask(outgoing) }
        return outgoing
    }

    private fun sendTask(outgoing: OutgoingMessage) {
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
            disconnect();
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

        val message = MessagesIO.readMessage(inputStream!!)
        Log.d(TAG, "Received message $message")

        if (message != null) {
            mExecutor.submit { client.onMessageReceived(message) }
        }
    }

    /**
     * This function is ran on mOutputThread, NOT ON mExecutor
     */
    private fun outputThreadTask() {
        Log.d(TAG, "Dequeueing from output queue")
        val outgoing = mOutputQueue.poll(Long.MAX_VALUE, TimeUnit.HOURS) ?: return

        try {
            Log.d(TAG, "Trying to write to output stream")

            MessagesIO.writeMessage(outgoing.message, outputStream!!)
            outputStream?.flush()

            mExecutor.submit { outgoing.response.complete(outgoing.message) }
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
                        Log.e(TAG, "", th)
                    } catch (th: InterruptedException) {
                        Log.e(TAG, "", th)
                    }
                }
            } catch (th: Throwable) {
                Log.e(TAG, "IO thread dead", th)
            }
        }
    }

}