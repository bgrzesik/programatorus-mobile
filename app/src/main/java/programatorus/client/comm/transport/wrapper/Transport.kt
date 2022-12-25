package programatorus.client.comm.transport.wrapper

import android.util.Log
import programatorus.client.comm.transport.*
import programatorus.client.utils.ActiveObject
import programatorus.client.utils.TaskRunner
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Wrapper class responsible for keeping ITransport logic sane.
 */

class Transport private constructor(
    transport: ITransportProvider,
    client: ITransportClient,
    private val mTaskRunner: TaskRunner,
    private val mClientTaskRunner: TaskRunner
) : ITransport, ActiveObject {

    companion object {
        private const val TAG = "Transport"
        private const val MAX_ERROR_COUNT = 4
        private const val RESCHEDULE_TIMEOUT = 100L
        private const val RECONNECT_TIMEOUT = 2000L
    }

    override val taskRunner: TaskRunner
        get() = mTaskRunner

    private var mTransportTaskPending = AtomicBoolean(false)
    private var mReconnectPending = AtomicBoolean(false)
    private var mDisconnectPending = AtomicBoolean(false)

    private val mPendingPacket = LinkedBlockingQueue<TransportOutgoingPacket>()
    private var mErrorCount = 0

    private var mClient: Client = Client(client)

    private var mImpl: ITransport = transport.build(mClient, mTaskRunner, mClientTaskRunner)

    override val state: ConnectionState
        get() {
            return if (mErrorCount >= MAX_ERROR_COUNT) {
                ConnectionState.ERROR
            } else {
                mImpl.state
            }
        }

    override fun send(packet: ByteArray): IOutgoingPacket {
        Log.d(TAG, "Enqueueing packet for sending pending=${mPendingPacket.size}")

        val transportOutgoingPacket = TransportOutgoingPacket(packet)
        mPendingPacket.add(transportOutgoingPacket)

        if (!mReconnectPending.get()) {
            scheduleTransportTask()
        }

        return transportOutgoingPacket
    }

    override fun disconnect() = runGuardedOnLooper(mDisconnectPending) {
        Log.d(TAG, "Disconnecting")
        mImpl.disconnect()
    }

    override fun reconnect() = reconnect(null)

    private fun reconnect(timeout: Long? = null) = runGuardedOnLooper(mReconnectPending, timeout) {
        Log.i(TAG, "reconnect()");
        mImpl.reconnect()
    }

    fun scheduleTransportTask(timeout: Long? = null): Unit =
        runGuardedOnLooper(mTransportTaskPending, timeout, true) {
            // Avoid not being able to schedule task if it's running
            mTransportTaskPending.set(false)

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

    private fun pumpPendingPackets() = assertLooper {
        Log.d(
            TAG,
            "pumpPendingPackets(): Pumping pending packets. pendingCount=${mPendingPacket.size}"
        )
        if (mPendingPacket.isEmpty()) {
            return@assertLooper
        }

        val outgoing = mPendingPacket.peek()!!
        if (outgoing.mPending) {
            return@assertLooper
        }

        Log.d(TAG, "pumpPendingPackets(): Sending packet and marking as pending")
        outgoing.mPending = true
        val implOutgoing = mImpl.send(outgoing.packet)
        outgoing.setOutgoingPacket(implOutgoing)
    }

    override fun toString(): String = "Transport[$mImpl]"

    private inner class TransportOutgoingPacket(
        override val packet: ByteArray
    ) : IOutgoingPacket {

        var mOutgoing: IOutgoingPacket? = null
            private set

        var mPending = false

        private val mCompletableFuture = CompletableFuture<IOutgoingPacket>()
        private var mOnComplete: CompletableFuture<IOutgoingPacket>? = null

        override val response: CompletableFuture<IOutgoingPacket>
            get() = mCompletableFuture

        fun setOutgoingPacket(outgoing: IOutgoingPacket?) = assertLooper {
            Log.d(TAG, "setOutgoingPacket()")
            if (mOnComplete != null) {
                Log.d(TAG, "setOutgoingPacket(): replacing future")
                val onComplete: CompletableFuture<*> = mOnComplete!!
                mOnComplete = null
                onComplete.cancel(true)
            }

            mOutgoing = outgoing
            mOnComplete = outgoing?.response
            mOnComplete = mOnComplete?.whenComplete(this::onComplete)
        }

        private fun onComplete(outgoing: IOutgoingPacket?, throwable: Throwable?) = runOnLooper {
            if (mOnComplete == null) {
                return@runOnLooper
            }

            Log.d(TAG, "onComplete() success=${outgoing != null} exception=${throwable != null}")
            outgoing?.let { onDelivered(it) }
            throwable?.let { onDeliveryFailed(it) }
        }

        private fun onDelivered(outgoing: IOutgoingPacket?) = assertLooper {
            assert(mOutgoing == outgoing)

            Log.d(TAG, "onDelivered(): Packet delivered.")

            runOnLooper(target = mClientTaskRunner) {
                mCompletableFuture.complete(outgoing)
            }

            val transportOutgoing = mPendingPacket.poll()!!

            assert(transportOutgoing.mOutgoing == outgoing)

            mErrorCount = 0
            scheduleTransportTask()
        }

        private fun onDeliveryFailed(throwable: Throwable) = assertLooper {
            Log.e(TAG, "onDeliveryFailed(): Packet delivery failure", throwable)

            runOnLooper(target = mClientTaskRunner) {
                mCompletableFuture.completeExceptionally(throwable)
            }

            mErrorCount++
            scheduleTransportTask()
        }
    }

    private inner class Client(
        private val mClient: ITransportClient
    ) : ITransportClient {

        private var mLastState: ConnectionState? = null

        override fun onStateChanged(state: ConnectionState) = runOnLooper {
            if (mLastState == state) {
                Log.d(TAG, "onStateChanged(): Discarding onStateChanged")
                // Don't report to client the same state twice in the row
                return@runOnLooper
            }
            mLastState = state

            scheduleTransportTask()
            runOnLooper(target = mClientTaskRunner) {
                mClient.onStateChanged(state)
            }
        }

        override fun onPacketReceived(packet: ByteArray) = runOnLooper {
            runOnLooper(target = mClientTaskRunner) {
                mClient.onPacketReceived(packet)
            }
        }

        override fun onError() = runOnLooper {
            mErrorCount++
            scheduleTransportTask()

            runOnLooper(target = mClientTaskRunner) {
                mClient.onError()
            }
        }
    }

    class Builder : AbstractTransportBuilder<Builder>() {
        private var mTransport: ITransportProvider? = null

        fun setTransport(transport: ITransportProvider): Builder {
            mTransport = transport
            return this
        }

        override fun construct(
            client: ITransportClient,
            taskRunner: TaskRunner,
            clientTaskRunner: TaskRunner
        ): ITransport = Transport(mTransport!!, client, taskRunner, clientTaskRunner)
    }
}