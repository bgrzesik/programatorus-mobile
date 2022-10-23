package programatorus.client.comm

import programatorus.client.comm.transport.ConnectionState

interface IConnectionClient {
    fun onStateChanged(state: ConnectionState) {}

    // TODO(bgrzesik): Remove this in favor of onStateChanged(ConnectionState::ERROR)
    fun onError() {}
}