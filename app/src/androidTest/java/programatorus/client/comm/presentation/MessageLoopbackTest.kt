package programatorus.client.comm.presentation

import android.util.Log
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.client.comm.TestUtils
import programatorus.client.comm.presentation.mock.LoopbackMessenger
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
        private const val TAG = "MessageLoopbackTest"

        private fun test(v: (IMessageClient) -> IMessenger): Array<Array<Any>> {
            val counts = arrayOf(0, 1, 10, 100)
            val name = TestUtils.getMessengerName(v)
            return counts.map { arrayOf(name, v, it) }.toTypedArray()
        }

        @JvmStatic
        @Parameterized.Parameters(name = "{0} x {2}")
        @Suppress("NestedLambdaShadowedImplicitParameter")
        internal fun parameters(): Array<Array<Any>> = arrayOf(
            // @formatter:off
            *test { ProtocolMessenger({ Transport(::PipedTransport, it) }, it) },
            *test { ProtocolMessenger({ PipedTransport(it) }, it) },
            *test { ProtocolMessenger({ Transport(::LoopbackTransport, it) }, it) },
            *test { ProtocolMessenger({ LoopbackTransport(it) }, it) },
            *test { LoopbackMessenger(it) },
            *test { Messenger({ ProtocolMessenger({ Transport(::PipedTransport, it) }, it) }, it) },
            *test { Messenger({ ProtocolMessenger({ PipedTransport(it) }, it) }, it) },
            *test { Messenger({ ProtocolMessenger({ Transport(::LoopbackTransport, it) }, it) }, it) },
            *test { Messenger({ ProtocolMessenger({ LoopbackTransport(it) }, it) }, it) },
            *test { Messenger({ LoopbackMessenger(it) }, it) },
            // @formatter:on
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

    @Test(timeout = 8000)
    fun testLoopbackSendReceive() {
        val queue = LinkedBlockingQueue<Protocol.GenericMessage>()

        val client = object : IMessageClient {
            override fun onMessageReceived(message: Protocol.GenericMessage) {
                Log.i(TAG, "onMessageReceived(): message.payloadCase=${message.payloadCase}")
                queue.add(message)
            }

            override fun onError() = Assert.fail("Transport Client caught error")
        }

        val transport = mProvider(client)

        val messages = mutableListOf<Protocol.GenericMessage>()
        for (i in 0 until mCount) {
            val message = Protocol.GenericMessage.newBuilder()
                .setRequestId(i.toLong())
                .setSessionId(i.toLong())
                .setTest(
                    Protocol.TestMessage.newBuilder()
                        .setValue("Test Message $i")
                )
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