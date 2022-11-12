package programatorus.client.comm.app

import programus.proto.Protocol.GenericMessage
import java.util.concurrent.CompletableFuture

interface PojoResponder<Request, Response> : IResponder {

    abstract fun unpackRequest(request: GenericMessage): Request

    abstract fun onRequest(request: Request): CompletableFuture<Response>

    abstract fun prepareResponse(response: Response): GenericMessage.Builder

    override fun onRequest(message: GenericMessage): CompletableFuture<GenericMessage> {
        assert(requestPayloadCase == message.payloadCase)
        val request = unpackRequest(message)
        return onRequest(request).thenApply { prepareResponse(it).build() }
    }


}