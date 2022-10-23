package programatorus.client.comm.presentation.mock

import programus.proto.Protocol

interface IMockMessengerEndpoint {

    fun onMessage(packet: Protocol.GenericMessage): Protocol.GenericMessage?

}