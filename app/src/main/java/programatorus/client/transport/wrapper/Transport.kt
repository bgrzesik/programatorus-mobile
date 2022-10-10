package programatorus.client.transport.wrapper

import android.util.Log
import programatorus.client.WeakRefFactoryMixin
import programatorus.client.transport.ConnectionState
import programatorus.client.transport.IOutgoingPacket
import programatorus.client.transport.ITransport
import programatorus.client.transport.ITransportClient
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
        private const val RECONNECT_TIMEOUT = 2000L
    }


    private var mTransportTaskFuture: Future<*>? = null
    private var mReconnectFuture: Future<*>? = null
    private var mDisconnectFuture: Future<*>? = null

    private val mPendingPacket: Queue<TransportOutgoingPacket> = ArrayDeque()
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

    override fun send(packet: ByteArray): IOutgoingPacket {
        Log.d(TAG, "Enqueueing packet for sending size=${packet.size}")

        val transportOutgoingPacket = TransportOutgoingPacket(weakRefFromThis(), packet)
        mPendingPacket.add(transportOutgoingPacket)

        if (mReconnectFuture == null || mReconnectFuture!!.isDone) {
            scheduleTransportTask()
        }

        return transportOutgoingPacket
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
                // Transport is disconnected, reconnect, then send pending packages
                mErrorCount++
                if (mErrorCount < MAX_ERROR_COUNT) {
                    reconnect(RECONNECT_TIMEOUT)
                }
            }
            ConnectionState.CONNECTED -> {
                pumpPendingPackets()
            }
        }
    }

    private fun pumpPendingPackets() {
        Log.d(TAG, "Pumping pending packets. pendingCount=${mPendingPacket.size}")
        if (mPendingPacket.isEmpty()) {
            return
        }

        val outgoing = mPendingPacket.peek()!!
        if (!outgoing.mPending) {
            return
        }

        outgoing.mPending = true
        val implOutgoing = mImpl.send(outgoing.packet)
        outgoing.setOutgoingPacket(implOutgoing)
    }

    private class TransportOutgoingPacket(
        private val mWeakTransport: WeakReference<Transport>,
        override val packet: ByteArray
    ) : IOutgoingPacket {

        var mOutgoing: IOutgoingPacket? = null
            private set

        var mPending = true

        private val mCompletableFuture = CompletableFuture<IOutgoingPacket>()
        private var mOnComplete: CompletableFuture<*>? = null

        override val response: CompletableFuture<IOutgoingPacket>
            get() = mCompletableFuture

        fun setOutgoingPacket(outgoing: IOutgoingPacket?) {
            if (mOnComplete != null) {
                val onComplete: CompletableFuture<*> = mOnComplete!!
                mOnComplete = null
                onComplete.cancel(true)
            }

            this.mOutgoing = outgoing
            mOnComplete = outgoing?.response?.whenComplete(this::onComplete)
        }

        private fun onComplete(outgoing: IOutgoingPacket?, throwable: Throwable?) {
            if (mOnComplete == null) {
                return
            }
            if (throwable == null) {
                onDelivered(outgoing!!)
            } else {
                onDeliveryFailed(throwable)
            }
        }

        private fun onDelivered(outgoing: IOutgoingPacket?) {
            assert(outgoing == outgoing)
            Log.d(TAG, "Packet delivered. packetSize=${packet.size}")
            mCompletableFuture.complete(outgoing)

            val transport = mWeakTransport.get()!!
            val transportOutgoing = transport.mPendingPacket.poll()!!

            assert(transportOutgoing.mOutgoing == outgoing)

            transport.mErrorCount = 0
            transport.scheduleTransportTask()
        }

        private fun onDeliveryFailed(throwable: Throwable) {
            Log.d(TAG, "Packet delivery failure. packetSize=${packet.size} exception=$throwable")
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

        override fun onPacketReceived(packet: ByteArray) =
            mClient.onPacketReceived(packet)

        override fun onError() {
            val transport = mWeakTransport.get()!!

            transport.mErrorCount++
            transport.scheduleTransportTask()

            mClient.onError()
        }
    }
}