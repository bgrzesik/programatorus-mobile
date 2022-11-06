package programatorus.client.comm.presentation

import android.os.Handler
import android.os.Looper
import android.util.Log
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.utils.HandlerActor
import programus.proto.Protocol
import java.util.concurrent.CompletableFuture

class Messenger(
    messengerProvider: (IMessageClient) -> IMessenger,
    client: IMessageClient,
    private val mHandler: Handler = Handler(Looper.getMainLooper()),
    private val mClientHandler: Handler = Handler(Looper.getMainLooper())
) : IMessenger, HandlerActor {

    companion object {
        const val TAG = "Messenger"
    }

    private val mClient = Client(client)
    private val mImpl: IMessenger = messengerProvider(mClient)

    override val handler: Handler
        get() = mHandler


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
            runOnLooper(null, false, mClientHandler) {
                assert(mOutgoing == outgoing)
                Log.d(TAG, "onDelivered(): Packet delivered.")
                mClientHandler.post { response.complete(mOutgoing) }
            }

        private fun onDeliveryFailed(throwable: Throwable) =
            runOnLooper(null, false, mClientHandler) {
                Log.e(TAG, "onDeliveryFailed(): Packet delivery failure", throwable)
                response.completeExceptionally(throwable)
            }

    }

    override fun toString(): String = "Messenger[$mImpl]"

    private inner class Client(
        private val mClient: IMessageClient,
    ) : IMessageClient {

        override fun onMessageReceived(message: Protocol.GenericMessage) =
            runOnLooper(null, false, mClientHandler) {
                mClientHandler.post { mClient.onMessageReceived(message) }
            }

        override fun onStateChanged(state: ConnectionState) =
            runOnLooper(null, false, mClientHandler) {
                mClient.onStateChanged(state)
            }

        override fun onError() =
            runOnLooper(null, false, mClientHandler) {
                mClient.onError()
            }
    }

}