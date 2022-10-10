package programatorus.client.transport

enum class ConnectionState {
    CONNECTING,

    CONNECTED,

    DISCONNECTING,

    DISCONNECTED,

    ERROR,
}