package programatorus.client.comm.presentation

import programatorus.client.comm.transport.IOutgoingPacket
import programus.proto.Protocol
import java.util.concurrent.CompletableFuture

interface IOutgoingMessage {

    val message: Protocol.GenericMessage

    val response: CompletableFuture<IOutgoingMessage>

}