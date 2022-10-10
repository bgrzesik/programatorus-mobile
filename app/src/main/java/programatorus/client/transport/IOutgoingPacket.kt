package programatorus.client.transport

import java.util.concurrent.CompletableFuture

interface IOutgoingPacket {

    // TODO(bgrzesik): add unique ID

    val packet: ByteArray

    val response: CompletableFuture<IOutgoingPacket>

}