package programatorus.client.comm.app

import programus.proto.Protocol
import programus.proto.Protocol.*
import programus.proto.Protocol.GenericMessage.*
import java.util.concurrent.CompletableFuture

interface IResponder {

    val requestPayloadCase: PayloadCase

    fun onRequest(message: GenericMessage): CompletableFuture<GenericMessage>;

}