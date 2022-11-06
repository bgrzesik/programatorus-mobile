package programatorus.client.comm.presentation

import programatorus.client.comm.IConnectionClient
import programus.proto.Protocol

interface IMessageClient : IConnectionClient {

    fun onMessageReceived(message: Protocol.GenericMessage) {}

}