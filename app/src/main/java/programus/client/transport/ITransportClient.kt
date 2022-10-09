package programus.client.transport

import programus.proto.GenericMessage

interface ITransportClient {

    fun onStateChanged(state: ConnectionState) {}

    // TODO(bgrzesik): Remove this in favor of onStateChanged(ConnectionState::ERROR)
    fun onError() {}

    fun onMessageReceived(message: GenericMessage) {}

}