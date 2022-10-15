package programatorus.client.comm.transport.wrapper

import programatorus.client.comm.transport.IOutgoingPacket
import java.util.concurrent.CompletableFuture

class OutgoingPacket (
   override val packet: ByteArray,
   override val response: CompletableFuture<IOutgoingPacket> = CompletableFuture()
): IOutgoingPacket