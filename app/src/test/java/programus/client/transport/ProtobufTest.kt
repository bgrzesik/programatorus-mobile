package programus.client.transport

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import programus.proto.GenericMessage
import programus.proto.TestMessage
import java.io.*
import java.util.concurrent.CountDownLatch


class ProtobufTest {

    @Before
    fun mockLog() = TestUtils.mockLog()

    @Test
    fun testBufferParseWrite() {
        val outputStream = ByteArrayOutputStream(1024)
        val message = TestUtils.newTestMessage()

        message.writeTo(outputStream)

        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val received = GenericMessage.parseFrom(inputStream)

        assert(message == received)
    }

}