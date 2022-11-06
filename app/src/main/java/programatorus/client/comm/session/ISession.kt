package programatorus.client.comm.session

import programatorus.client.comm.IConnection
import programus.proto.Protocol
import java.util.concurrent.CompletableFuture

interface ISession : IConnection {

    fun request(message: Protocol.GenericMessage): CompletableFuture<Protocol.GenericMessage>

}