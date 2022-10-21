package programatorus.mobile.transport

import programatorus.client.comm.transport.ITransport
import programatorus.client.comm.transport.ITransportClient
import programus.proto.Protocol

typealias TransportProvider = (ITransportClient) -> ITransport

object TestUtils {
    fun newTestMessage() = Protocol.GenericMessage.newBuilder()
        .setSessionId(10)
        .setTest(Protocol.TestMessage.newBuilder().apply {
            value = "Test 1234"
        })
        .build()
}