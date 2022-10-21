package programatorus.client.comm.transport.mock

interface MockTransportEndpoint {

    fun onPacket(packet: ByteArray): ByteArray?

}