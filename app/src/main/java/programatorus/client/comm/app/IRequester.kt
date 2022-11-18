package programatorus.client.comm.app

import programatorus.client.comm.session.ISession
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase
import java.util.concurrent.CompletableFuture

interface IRequester<Out> {
    fun prepareRequest(): GenericMessage.Builder

    val responsePayloadCase: PayloadCase

    fun handleResponse(message: GenericMessage): Out

    private fun onResponse(response: GenericMessage): Out {
        if (responsePayloadCase != response.payloadCase) {
            throw InvalidResponseException(responsePayloadCase, response.payloadCase)
        }
        return handleResponse(response)
    }

    fun request(session: ISession): CompletableFuture<Out> {
        val message = prepareRequest().build()
        assert(message.payloadCase != PayloadCase.PAYLOAD_NOT_SET)
        return session.request(message)
            .thenApply(this::onResponse)
    }

}