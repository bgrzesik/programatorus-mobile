package programatorus.client.comm

import programatorus.client.comm.transport.ConnectionState

interface IConnectionClient {
    fun onStateChanged(state: ConnectionState) {}

    fun onError() {}
}