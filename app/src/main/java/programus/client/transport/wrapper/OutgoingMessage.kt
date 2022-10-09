package programus.client.transport.wrapper

import programus.client.transport.IOutgoingMessage
import programus.proto.GenericMessage
import java.util.concurrent.CompletableFuture

class OutgoingMessage (
   override val message: GenericMessage,
   override val response: CompletableFuture<GenericMessage> = CompletableFuture()
): IOutgoingMessage