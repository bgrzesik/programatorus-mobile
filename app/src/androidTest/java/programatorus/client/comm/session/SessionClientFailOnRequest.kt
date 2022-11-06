package programatorus.client.comm.session

import org.junit.Assert
import programus.proto.Protocol
import java.util.concurrent.CompletableFuture

interface SessionClientFailOnRequest : ISessionClient {

    override fun onRequest(request: Protocol.GenericMessage): CompletableFuture<Protocol.GenericMessage> {
        Assert.fail("Unexpected message $request")
        return CompletableFuture<Protocol.GenericMessage>().apply {
            completeExceptionally(RuntimeException("Unexpected"))
        }
    }

}