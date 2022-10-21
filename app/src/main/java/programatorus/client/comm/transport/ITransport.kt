package programatorus.client.comm.transport

import programatorus.client.comm.IConnection

interface ITransport : IConnection {

    fun send(packet: ByteArray): IOutgoingPacket

}