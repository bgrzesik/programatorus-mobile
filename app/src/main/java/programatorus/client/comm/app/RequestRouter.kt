package programatorus.client.comm.app

import android.util.Log
import programatorus.client.comm.IConnectionClient
import programatorus.client.comm.session.ISessionClient
import programatorus.client.comm.transport.ConnectionState
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase
import java.util.concurrent.CompletableFuture

class RequestRouter(
    responders: List<IResponder>,
    private var mConnectionClient: IConnectionClient? = null
) : ISessionClient {
    private companion object {
        const val TAG = "RequestRouter"
    }


    private val mResponders: Map<PayloadCase, IResponder> =
        responders.associateBy { it.requestPayloadCase }

    init {
        for ((key, _) in mResponders) {
            Log.d(TAG, "init(): Registered responder for $key")
        }
    }

    override fun onRequest(request: GenericMessage): CompletableFuture<GenericMessage> {
        val responder = mResponders[request.payloadCase]
        if (responder == null) {
            Log.e(TAG, "onRequest(): Missing Responder for ${request.payloadCase}")
            val exc = UnsupportedRequestException(request.payloadCase)
            return CompletableFuture<GenericMessage>().apply {
                completeExceptionally(exc)
            }
        }
        Log.i(TAG, "onRequest(): Defering request ${request.payloadCase} to $responder")
        return responder.onRequest(request)
    }

    override fun onStateChanged(state: ConnectionState) {
        mConnectionClient?.onStateChanged(state)
    }

    override fun onError() {
        mConnectionClient?.onError()
    }

}