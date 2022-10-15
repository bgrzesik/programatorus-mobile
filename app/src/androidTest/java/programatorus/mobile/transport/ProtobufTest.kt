package programatorus.mobile.transport

import org.junit.Test
import programus.proto.Protocol
import java.io.*


class ProtobufTest {

    @Test
    fun testBufferParseWrite() {
        val outputStream = ByteArrayOutputStream(1024)
        val message = TestUtils.newTestMessage()

        message.writeTo(outputStream)

        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val received = Protocol.GenericMessage.parseFrom(inputStream)

        assert(message == received)
    }

}