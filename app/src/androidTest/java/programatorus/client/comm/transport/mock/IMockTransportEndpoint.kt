package programatorus.client.comm.transport.mock

interface IMockTransportEndpoint {

    fun onPacket(packet: ByteArray): ByteArray?

}