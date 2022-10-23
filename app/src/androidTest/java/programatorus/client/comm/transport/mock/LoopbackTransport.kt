package programatorus.client.comm.transport.mock

import programatorus.client.comm.transport.ITransportClient

class LoopbackTransport(
    client: ITransportClient
) : MockTransport(Endpoint(), client) {

    override fun toString(): String = "LoopbackTransport"

    private class Endpoint : IMockTransportEndpoint {
        override fun onPacket(packet: ByteArray): ByteArray = packet
    }

}