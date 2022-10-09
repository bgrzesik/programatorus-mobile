package programus.client.transport

import programus.proto.GenericMessage

interface ITransport {

    val state: ConnectionState

    fun send(message: GenericMessage): IOutgoingMessage

    // TODO(bgrzesik): Return future
    fun reconnect()

    // TODO(bgrzesik): Return future
    fun disconnect()

}