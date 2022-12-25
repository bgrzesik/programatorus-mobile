package programatorus.client.comm

import programatorus.client.comm.transport.ConnectionState

interface IConnection {
    val state: ConnectionState

    fun reconnect()

    fun disconnect()
}