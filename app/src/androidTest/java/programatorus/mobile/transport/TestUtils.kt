package programatorus.mobile.transport

import programus.proto.GenericMessage
import programus.proto.TestMessage

object TestUtils {
    fun newTestMessage() = GenericMessage.newBuilder()
        .setSessionId(10)
        .setTest(TestMessage.newBuilder().apply {
            value = "Test 1234"
        })
        .build()

}