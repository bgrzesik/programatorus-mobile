package programatorus.client.comm.transport

import programatorus.client.comm.IConnectionClient

interface ITransportClient : IConnectionClient {

    fun onPacketReceived(packet: ByteArray) {}

}