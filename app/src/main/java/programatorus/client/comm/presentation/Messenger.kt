package programatorus.client.comm.presentation

import android.util.Log
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.utils.Tasker
import programatorus.client.utils.TaskRunner
import programus.proto.Protocol
import java.util.concurrent.CompletableFuture

class Messenger private constructor(
    messenger: IMessengerProvider,
    client: IMessageClient,
    private val mTaskRunner: TaskRunner,
    private val mClientTaskRunner: TaskRunner
) : IMessenger, Tasker {

    companion object {
        const val TAG = "Messenger"
    }

    private val mClient = Client(client)
    private val mImpl: IMessenger = messenger.build(mClient, mTaskRunner, mClientTaskRunner)

    override val taskRunner: TaskRunner
        get() = mTaskRunner


    override fun send(message: Protocol.GenericMessage): IOutgoingMessage {
        val outgoing = OutgoingMessage(message)

        runOnLooper {
            val implOutgoing = mImpl.send(message)
            outgoing.setOutgoingMessage(implOutgoing)
        }

        return outgoing
    }

    override val state: ConnectionState
        get() = mImpl.state

    override fun reconnect() = runOnLooper { mImpl.reconnect() }

    override fun disconnect() = runOnLooper { mImpl.disconnect() }

    private inner class OutgoingMessage(
        override val message: Protocol.GenericMessage
    ) : IOutgoingMessage {

        private var mOutgoing: IOutgoingMessage? = null;
        private var mOnComplete: CompletableFuture<IOutgoingMessage>? = null

        override val response: CompletableFuture<IOutgoingMessage> = CompletableFuture()

        fun setOutgoingMessage(outgoing: IOutgoingMessage) {
            Log.d(TAG, "setOutgoingMessage()")
            if (mOnComplete != null) {
                Log.d(TAG, "setOutgoingMessage(): replace future")
                val onComplete: CompletableFuture<*> = mOnComplete!!
                mOnComplete = null
                onComplete.cancel(true)
            }

            mOutgoing = outgoing
            mOnComplete = outgoing.response
            mOnComplete = mOnComplete?.whenComplete(this::onComplete)
        }

        private fun onComplete(outgoing: IOutgoingMessage?, throwable: Throwable?) = runOnLooper {
            if (mOnComplete == null) {
                return@runOnLooper
            }

            Log.d(TAG, "onComplete() success=${outgoing != null} exception=${throwable != null}")
            outgoing?.let { onDelivered(it) }
            throwable?.let { onDeliveryFailed(it) }
        }

        private fun onDelivered(outgoing: IOutgoingMessage?) =
            runOnLooper(target = mClientTaskRunner) {
                assert(mOutgoing == outgoing)
                Log.d(TAG, "onDelivered(): Packet delivered.")
                response.complete(mOutgoing)
            }

        private fun onDeliveryFailed(throwable: Throwable) =
            runOnLooper(target = mClientTaskRunner) {
                Log.e(TAG, "onDeliveryFailed(): Packet delivery failure", throwable)
                response.completeExceptionally(throwable)
            }

    }

    override fun toString(): String = "Messenger[$mImpl]"

    private inner class Client(
        private val mClient: IMessageClient,
    ) : IMessageClient {

        override fun onMessageReceived(message: Protocol.GenericMessage) =
            runOnLooper(target = mClientTaskRunner) {
                mClient.onMessageReceived(message)
            }

        override fun onStateChanged(state: ConnectionState) =
            runOnLooper(target = mClientTaskRunner) {
                mClient.onStateChanged(state)
            }

        override fun onError() =
            runOnLooper(target = mClientTaskRunner) {
                mClient.onError()
            }
    }

    class Builder : AbstractMessengerBuilder<Builder>() {
        private var mMessenger: IMessengerProvider? = null

        fun setMessenger(messenger: IMessengerProvider): Builder {
            mMessenger = messenger
            return this
        }

        override fun construct(
            client: IMessageClient,
            taskRunner: TaskRunner,
            clientTaskRunner: TaskRunner
        ): IMessenger = Messenger(mMessenger!!, client, taskRunner, clientTaskRunner)
    }

}