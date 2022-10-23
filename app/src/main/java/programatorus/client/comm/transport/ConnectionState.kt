package programatorus.client.comm.transport

enum class ConnectionState {
    CONNECTING,

    CONNECTED,

    DISCONNECTING,

    DISCONNECTED,

    ERROR,
}