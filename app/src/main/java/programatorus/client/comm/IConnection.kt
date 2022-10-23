package programatorus.client.comm

import programatorus.client.comm.transport.ConnectionState

interface IConnection {
    val state: ConnectionState

    // TODO(bgrzesik): Return future
    fun reconnect()

    // TODO(bgrzesik): Return future
    fun disconnect()
}