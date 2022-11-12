package programatorus.client.comm.presentation

import org.junit.Test
import programatorus.client.comm.TestUtils
import programus.proto.Protocol
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


class ProtobufTest {

    @Test(timeout=500)
    fun testBufferParseWrite() {
        val outputStream = ByteArrayOutputStream(1024)
        val message = TestUtils.newTestMessage()

        message.writeTo(outputStream)

        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val received = Protocol.GenericMessage.parseFrom(inputStream)

        assert(message == received)
    }

}