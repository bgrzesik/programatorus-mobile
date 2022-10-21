package programatorus.client.comm.transport

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.client.comm.transport.io.PipedTransport
import programatorus.client.comm.transport.mock.LoopbackTransport
import programatorus.client.comm.transport.wrapper.Transport
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@RunWith(Parameterized::class)
open class SendLoopbackTest(
    @Suppress("unused")
    private var mTestName: String,
    private var mProvider: TransportProvider,
    private var mCount: Int
) {
    companion object {
        private fun p(v: TransportProvider) = v

        @JvmStatic
        @Parameterized.Parameters(name = "{0} x {2}")
        internal fun parameters(): Array<Array<Any>> = arrayOf(
            arrayOf("Transport(Piped)", p { Transport(::PipedTransport, it) }, 100),
            arrayOf("Transport(Piped)", p { Transport(::PipedTransport, it) }, 10),
            arrayOf("Transport(Piped)", p { Transport(::PipedTransport, it) }, 1),
            arrayOf("Transport(Piped)", p { Transport(::PipedTransport, it) }, 0),

            arrayOf("Piped", p { PipedTransport(it) }, 100),
            arrayOf("Piped", p { PipedTransport(it) }, 10),
            arrayOf("Piped", p { PipedTransport(it) }, 1),
            arrayOf("Piped", p { PipedTransport(it) }, 0),

            arrayOf("Transport(Loopback)", p { Transport(::LoopbackTransport, it) }, 100),
            arrayOf("Transport(Loopback)", p { Transport(::LoopbackTransport, it) }, 10),
            arrayOf("Transport(Loopback)", p { Transport(::LoopbackTransport, it) }, 1),
            arrayOf("Transport(Loopback)", p { Transport(::LoopbackTransport, it) }, 0),

            arrayOf("Loopback", p { LoopbackTransport(it) }, 100),
            arrayOf("Loopback", p { LoopbackTransport(it) }, 10),
            arrayOf("Loopback", p { LoopbackTransport(it) }, 1),
            arrayOf("Loopback", p { LoopbackTransport(it) }, 0),
        )
    }

    @Before
    open fun assumeRunOnAndroid() {
        Assume.assumeThat(
            "Those tests should be ran on Android",
            System.getProperty("java.specification.vendor"),
            `is`("The Android Project")
        )
    }

    @Test(timeout = 5000)
    fun testLoopbackSendReceive() {
        val queue = LinkedBlockingQueue<ByteArray>()

        val client = object : ITransportClient {
            override fun onPacketReceived(packet: ByteArray) {
                println(packet.decodeToString())
                queue.add(packet)
            }

            override fun onError() = Assert.fail("Transport Client caught error")
        }

        val transport = mProvider(client)

        val messages = mutableListOf<ByteArray>()
        for (i in 0..mCount) {
            val message = "Test $i message".toByteArray()
            messages.add(message)
            transport.send(message)
        }

        for (message in messages) {
            val received = queue.poll(Long.MAX_VALUE, TimeUnit.DAYS)
            Assert.assertNotNull(received)
            Assert.assertArrayEquals(message, received)
        }

        Assert.assertNull(
            "Received more messages then expected",
            queue.poll(1000, TimeUnit.MILLISECONDS)
        )

        transport.disconnect()
    }
}