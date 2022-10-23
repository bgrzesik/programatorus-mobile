package programatorus.client.comm.presentation

import programatorus.client.comm.IConnection
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.comm.transport.IOutgoingPacket
import programus.proto.Protocol

interface IMessenger : IConnection {

    fun send(message: Protocol.GenericMessage): IOutgoingMessage

}