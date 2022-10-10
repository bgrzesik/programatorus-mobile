package programatorus.client.transport

interface ITransportClient {

    fun onStateChanged(state: ConnectionState) {}

    // TODO(bgrzesik): Remove this in favor of onStateChanged(ConnectionState::ERROR)
    fun onError() {}

    fun onPacketReceived(packet: ByteArray) {}

}