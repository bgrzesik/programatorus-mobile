package programatorus.client.comm.presentation

import com.google.protobuf.Parser
import programatorus.client.comm.IConnectionClient
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.comm.transport.ITransportClient
import programus.proto.Protocol

interface IMessageClient : IConnectionClient {

    fun onMessageReceived(message: Protocol.GenericMessage) {}

}