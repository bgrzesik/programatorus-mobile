package programatorus.client.comm.app

import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase
import java.util.concurrent.CompletableFuture

interface IResponder {

    val requestPayloadCase: PayloadCase

    fun onRequest(message: GenericMessage): CompletableFuture<GenericMessage>;

}