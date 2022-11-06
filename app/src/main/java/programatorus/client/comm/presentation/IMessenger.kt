package programatorus.client.comm.presentation

import programatorus.client.comm.IConnection
import programus.proto.Protocol

interface IMessenger : IConnection {

    fun send(message: Protocol.GenericMessage): IOutgoingMessage

}