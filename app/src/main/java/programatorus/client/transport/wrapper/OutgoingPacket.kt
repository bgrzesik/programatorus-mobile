package programatorus.client.transport.wrapper

import programatorus.client.transport.IOutgoingPacket
import java.util.concurrent.CompletableFuture

class OutgoingPacket (
   override val packet: ByteArray,
   override val response: CompletableFuture<IOutgoingPacket> = CompletableFuture()
): IOutgoingPacket