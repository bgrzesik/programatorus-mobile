package programus.client.transport

import programus.proto.GenericMessage
import java.util.concurrent.CompletableFuture

interface IOutgoingMessage {

    val message: GenericMessage

    val response: CompletableFuture<GenericMessage>

}