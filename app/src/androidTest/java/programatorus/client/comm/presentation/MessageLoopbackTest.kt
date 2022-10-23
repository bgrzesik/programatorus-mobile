package programatorus.client.comm.presentation

import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.client.comm.presentation.mock.LoopbackMessenger
import programatorus.client.comm.transport.ITransport
import programatorus.client.comm.transport.ITransportClient
import programatorus.client.comm.transport.io.PipedTransport
import programatorus.client.comm.transport.mock.LoopbackTransport
import programatorus.client.comm.transport.wrapper.Transport
import programus.proto.Protocol
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@RunWith(Parameterized::class)
open class MessageLoopbackTest(
    private var mTestName: String,
    private var mProvider: (IMessageClient) -> IMessenger,
    private var mCount: Int
) {

    companion object {
        private fun p(v: (ITransportClient) -> ITransport): (IMessageClient) -> IMessenger =
            { Messenger(v, it) }

        private fun m(v: (IMessageClient) -> IMessenger) = v

        @JvmStatic
        @Parameterized.Parameters(name = "{0} x {2}")
        internal fun parameters(): Array<Array<Any>> = arrayOf(
            arrayOf("Messenger(Transport(PipedTransport))", p { Transport(::PipedTransport, it) }, 100),
            arrayOf("Messenger(Transport(PipedTransport))", p { Transport(::PipedTransport, it) }, 10),
            arrayOf("Messenger(Transport(PipedTransport))", p { Transport(::PipedTransport, it) }, 1),
            arrayOf("Messenger(Transport(PipedTransport))", p { Transport(::PipedTransport, it) }, 0),

            arrayOf("Messenger(PipedTransport)", p { PipedTransport(it) }, 100),
            arrayOf("Messenger(PipedTransport)", p { PipedTransport(it) }, 10),
            arrayOf("Messenger(PipedTransport)", p { PipedTransport(it) }, 1),
            arrayOf("Messenger(PipedTransport)", p { PipedTransport(it) }, 0),

            arrayOf("Messenger(Transport(LoopbackTransport))", p { Transport(::LoopbackTransport, it) }, 100),
            arrayOf("Messenger(Transport(LoopbackTransport))", p { Transport(::LoopbackTransport, it) }, 10),
            arrayOf("Messenger(Transport(LoopbackTransport))", p { Transport(::LoopbackTransport, it) }, 1),
            arrayOf("Messenger(Transport(LoopbackTransport))", p { Transport(::LoopbackTransport, it) }, 0),

            arrayOf("Messenger(LoopbackTransport)", p { LoopbackTransport(it) }, 100),
            arrayOf("Messenger(LoopbackTransport)", p { LoopbackTransport(it) }, 10),
            arrayOf("Messenger(LoopbackTransport)", p { LoopbackTransport(it) }, 1),
            arrayOf("Messenger(LoopbackTransport)", p { LoopbackTransport(it) }, 0),

            arrayOf("LoopbackMessenger", m { LoopbackMessenger(it) }, 100),
            arrayOf("LoopbackMessenger", m { LoopbackMessenger(it) }, 10),
            arrayOf("LoopbackMessenger", m { LoopbackMessenger(it) }, 1),
            arrayOf("LoopbackMessenger", m { LoopbackMessenger(it) }, 0),
        )
    }

    @Before
    open fun assumeRunOnAndroid() {
        Assume.assumeThat(
            "Those tests should be ran on Android",
            System.getProperty("java.specification.vendor"),
            CoreMatchers.`is`("The Android Project")
        )
    }

    @Test(timeout = 5000)
    fun testLoopbackSendReceive() {
        val queue = LinkedBlockingQueue<Protocol.GenericMessage>()

        val client = object : IMessageClient {
            override fun onMessageReceived(message: Protocol.GenericMessage) {
                queue.add(message)
            }

            override fun onError() = Assert.fail("Transport Client caught error")
        }

        val transport = mProvider(client)

        val messages = mutableListOf<Protocol.GenericMessage>()
        for (i in 0..mCount) {
            val message = Protocol.GenericMessage.newBuilder()
                .setRequestId(i.toLong())
                .setSessionId(i.toLong())
                .setTest(Protocol.TestMessage.newBuilder()
                    .setValue("Test Message $i"))
                .build()

            messages.add(message)
            transport.send(message)
        }

        for (message in messages) {
            val received = queue.poll(Long.MAX_VALUE, TimeUnit.DAYS)
            Assert.assertNotNull(received)
            Assert.assertEquals(message, received)
        }

        Assert.assertNull(
            "Received more messages then expected",
            queue.poll(1000, TimeUnit.MILLISECONDS)
        )

        transport.disconnect()
    }
}