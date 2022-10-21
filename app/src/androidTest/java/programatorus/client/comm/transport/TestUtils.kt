package programatorus.client.comm.transport

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