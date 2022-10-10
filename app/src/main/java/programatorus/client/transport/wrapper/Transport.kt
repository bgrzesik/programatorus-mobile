package programatorus.client.transport.wrapper

import android.util.Log
import programatorus.client.WeakRefFactoryMixin
import programatorus.client.transport.ConnectionState
import programatorus.client.transport.IOutgoingMessage
import programatorus.client.transport.ITransport
import programatorus.client.transport.ITransportClient
import programus.proto.GenericMessage
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.*

/**
 * Wrapper class responsible for keeping ITransport logic sane.
 * TODO(bgrzesik) wrap mImpl method calls with try/catch
 */

class Transport(
    implProvider: (ITransportClient, ScheduledExecutorService) -> ITransport,
    client: ITransportClient,
    // TODO(bgrzesik): Consider using looper/handler (idiomatic android way of doing this).
    private val mExecutor: ScheduledExecutorService = ScheduledThreadPoolExecutor(1)
) : ITransport, WeakRefFactoryMixin<Transport> {

    companion object {
        private const val TAG = "Transport"
        private const val MAX_ERROR_COUNT = 4
        private const val RESCHEDULE_TIMEOUT = 100L
        private const val RECONNECT_TIMEOUT = 2000L;
    }


    private var mTransportTaskFuture: Future<*>? = null
    private var mReconnectFuture: Future<*>? = null
    private var mDisconnectFuture: Future<*>? = null

    private val mPendingMessages: Queue<TransportOutgoingMessage> = ArrayDeque()
    private var mErrorCount = 0

    private var mClient: TransportClient = TransportClient(weakRefFromThis(), client)

    private var mImpl: ITransport = implProvider(mClient, mExecutor)

    override val state: ConnectionState
        get() {
            return if (mErrorCount >= MAX_ERROR_COUNT) {
                ConnectionState.ERROR
            } else {
                mImpl.state
            }
        }

    init {
        mClient.onStateChanged(state)
    }

    override fun send(message: GenericMessage): IOutgoingMessage {
        Log.d(TAG, "Enqueueing message for sending: $message")

        val transportOutgoingMessage = TransportOutgoingMessage(weakRefFromThis(), message)
        mPendingMessages.add(transportOutgoingMessage)
        scheduleTransportTask()

        return transportOutgoingMessage
    }

    override fun disconnect() {
        if (mDisconnectFuture != null && !mDisconnectFuture!!.isDone) {
            return
        }
        Log.d(TAG, "Requesting disconnect")
        mDisconnectFuture = mExecutor.submit(mImpl::disconnect)
    }

    override fun reconnect() = reconnect(null)

    private fun reconnect(timeout: Long? = null) {
        if (mReconnectFuture != null && !mReconnectFuture!!.isDone) {
            return
        }
        Log.d(TAG, "Requesting reconnect")
        mReconnectFuture = if (timeout != null) {
            mExecutor.schedule(mImpl::reconnect, timeout, TimeUnit.MILLISECONDS)
        } else {
            mExecutor.submit(mImpl::reconnect)
        }
    }

    fun scheduleTransportTask(timeout: Long? = null) {
        if (mTransportTaskFuture != null && !mTransportTaskFuture!!.isDone) {
            return
        }
        Log.d(TAG, "Scheduling transport task")

        mTransportTaskFuture = if (timeout != null) {
            mExecutor.schedule(this::transportTask, timeout, TimeUnit.MILLISECONDS)
        } else {
            mExecutor.submit(this::transportTask)
        }
    }

    private fun transportTask() {
        // Avoid not being able to schedule task if it's running
        mTransportTaskFuture = null

        val state = state
        Log.d(TAG, "Transport task running. state=$state")

        when (state) {
            ConnectionState.CONNECTING -> {
                // Transport is connecting, reschedule for later
                scheduleTransportTask(RESCHEDULE_TIMEOUT)
            }
            ConnectionState.ERROR -> {
                // Transport in error state, do nothing
                if (mErrorCount < MAX_ERROR_COUNT) {
                    reconnect(RECONNECT_TIMEOUT)
                }
            }
            ConnectionState.DISCONNECTING -> {
                // Transport is disconnecting, reschedule for later
                scheduleTransportTask(RESCHEDULE_TIMEOUT)
            }
            ConnectionState.DISCONNECTED -> {
                // Transport is disconnected, reconnect, then send pending messages
                mErrorCount++
                if (mErrorCount < MAX_ERROR_COUNT) {
                    reconnect(RECONNECT_TIMEOUT)
                }
            }
            ConnectionState.CONNECTED -> {
                pumpPendingMessages()
            }
        }
    }

    private fun pumpPendingMessages() {
        Log.d(TAG, "Pumping pending message. pendingCount=${mPendingMessages.size}")
        if (mPendingMessages.isEmpty()) {
            return
        }

        val outgoingMessage = mPendingMessages.peek()!!
        if (!outgoingMessage.pending) {
            return
        }

        outgoingMessage.pending = true
        val implOutgoing = mImpl.send(outgoingMessage.message)
        outgoingMessage.setOutgoingMessage(implOutgoing)
    }

    private class TransportOutgoingMessage(
        private val mWeakTransport: WeakReference<Transport>,
        override val message: GenericMessage
    ) : IOutgoingMessage {

        var outgoing: IOutgoingMessage? = null
            private set

        var pending = true

        private val mCompletableFuture = CompletableFuture<GenericMessage>()
        private var mOnComplete: CompletableFuture<*>? = null

        override val response: CompletableFuture<GenericMessage>
            get() = mCompletableFuture

        fun setOutgoingMessage(outgoingMessage: IOutgoingMessage?) {
            if (mOnComplete != null) {
                val onComplete: CompletableFuture<*> = mOnComplete!!
                mOnComplete = null
                onComplete.cancel(true)
            }

            this.outgoing = outgoingMessage
            mOnComplete = outgoingMessage?.response?.whenComplete(this::onComplete)
        }

        private fun onComplete(response: GenericMessage?, throwable: Throwable?) {
            if (mOnComplete == null) {
                return
            }
            if (throwable == null) {
                onMessageDelivered(response!!)
            } else {
                onMessageDeliveryFailed(throwable)
            }
        }

        private fun onMessageDelivered(response: GenericMessage) {
            assert(message == response)
            Log.d(TAG, "Message delivered. message=$message")
            mCompletableFuture.complete(response)

            val transport = mWeakTransport.get()!!
            val transportOutgoing = transport.mPendingMessages.poll()!!

            assert(transportOutgoing.outgoing == outgoing)

            transport.mPendingMessages.remove(this)
            transport.mErrorCount = 0
            transport.scheduleTransportTask()
        }

        private fun onMessageDeliveryFailed(throwable: Throwable) {
            Log.d(TAG, "Message delivery failure. message=$message exception=$throwable")
            val transport = mWeakTransport.get()!!
            mCompletableFuture.completeExceptionally(throwable)
            transport.mErrorCount++
            transport.scheduleTransportTask()
        }
    }

    private class TransportClient(
        private val mWeakTransport: WeakReference<Transport>,
        private val mClient: ITransportClient
    ) : ITransportClient {

        private var lastState: ConnectionState? = null

        override fun onStateChanged(state: ConnectionState) {
            if (lastState == state) {
                Log.d(TAG, "Discarding onStateChanged")
                // Don't report to client the same state twice in the row
                return
            }
            lastState = state

            val transport = mWeakTransport.get()!!

            mClient.onStateChanged(state)
            transport.scheduleTransportTask()
        }

        override fun onMessageReceived(message: GenericMessage) =
            mClient.onMessageReceived(message)

        override fun onError() {
            val transport = mWeakTransport.get()!!

            transport.mErrorCount++
            transport.scheduleTransportTask()

            mClient.onError()
        }
    }
}