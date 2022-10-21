package programatorus.client.comm.presentation

import android.util.Log
import programatorus.client.comm.ConnectionClientDelegate
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.comm.transport.IOutgoingPacket
import programatorus.client.comm.transport.ITransport
import programatorus.client.comm.transport.ITransportClient
import programus.proto.Protocol
import java.util.concurrent.CompletableFuture

class Messenger(
    transportProvider: (ITransportClient) -> ITransport,
    private val mClient: IMessageClient
) : IMessenger {

    private val mTransportClient = Client()
    private val mTransport: ITransport = transportProvider(mTransportClient)

    companion object {
        const val TAG = "Messenger"
    }

    override val state: ConnectionState
        get() = mTransport.state

    override fun send(message: Protocol.GenericMessage): IOutgoingMessage {
        Log.d(TAG, "send() ${message.payloadCase}")
        val packet = mTransport.send(message.toByteArray())
        return OutgoingMessage(message, packet)
    }

    override fun reconnect() = mTransport.reconnect()

    override fun disconnect() = mTransport.disconnect()

    class OutgoingMessage(
        override val message: Protocol.GenericMessage,
        outgoingPacket: IOutgoingPacket
    ) : IOutgoingMessage {
        override val response: CompletableFuture<IOutgoingMessage> = CompletableFuture()

        init {
            outgoingPacket.response.whenComplete { packet, exception ->
                exception?.let { response.completeExceptionally(it) }
                packet?.let { response.complete(this) }
            }
        }
    }

    private inner class Client : ConnectionClientDelegate(mClient), ITransportClient {
        override fun onPacketReceived(packet: ByteArray) =
            mClient.onMessageReceived(Protocol.GenericMessage.parseFrom(packet))
    }
}