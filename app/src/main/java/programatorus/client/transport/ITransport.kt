package programatorus.client.transport

interface ITransport {

    val state: ConnectionState

    fun send(packet: ByteArray): IOutgoingPacket

    // TODO(bgrzesik): Return future
    fun reconnect()

    // TODO(bgrzesik): Return future
    fun disconnect()

}