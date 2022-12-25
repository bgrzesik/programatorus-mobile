package programatorus.client.comm.transport

import java.util.concurrent.CompletableFuture

interface IOutgoingPacket {

    val packet: ByteArray

    val response: CompletableFuture<IOutgoingPacket>

}