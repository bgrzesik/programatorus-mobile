package programatorus.client.comm.session

import programatorus.client.comm.IConnectionClient
import programus.proto.Protocol
import java.util.concurrent.CompletableFuture

interface ISessionClient : IConnectionClient {

    fun onRequest(request: Protocol.GenericMessage): CompletableFuture<Protocol.GenericMessage>

}