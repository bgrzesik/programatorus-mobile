package programatorus.mobile.transport.io

import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import programatorus.client.transport.TestUtils
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.CountDownLatch

internal class MessagesIOTest {

    @Before
    fun mockLog() = TestUtils.mockLog()

    @Test
    fun testPipedParseWrite() {
        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()

        inputStream.connect(outputStream)

        val message = TestUtils.newTestMessage()
        val countDownLatch = CountDownLatch(2)

        val sendingThread = Thread {
            MessagesIO.writeMessage(message, outputStream)
            outputStream.flush()
            countDownLatch.countDown()
            countDownLatch.await()
        }

        sendingThread.start()

        val received = MessagesIO.readMessage(inputStream)
        assert(message == received)
        countDownLatch.countDown()
        countDownLatch.await()

        inputStream.close()
        outputStream.close()

        sendingThread.join()
    }

}