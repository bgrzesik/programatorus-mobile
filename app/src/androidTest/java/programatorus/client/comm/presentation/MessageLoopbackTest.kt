package programatorus.client.comm.presentation

import android.util.Log
import org.junit.Assert
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
    private var mProvider: IMessengerProvider,
    private var mCount: Int
) {

    companion object {
        private const val TAG = "MessageLoopbackTest"

        private fun test(messenger: IMessengerProvider): Array<Array<Any>> {
            val counts = arrayOf(0, 1, 10, 100)
            val name = TestUtils.getMessengerName(messenger)
            return counts.map { arrayOf(name, messenger, it) }.toTypedArray()
        }

        @JvmStatic
        @Parameterized.Parameters(name = "{0} x {2}")
        internal fun parameters(): Array<Array<Any>> = arrayOf(
            *test(
                ProtocolMessenger.Builder()
                    .setTransport(Transport.Builder().setTransport(PipedTransport.Builder()))
            ),
            *test(ProtocolMessenger.Builder().setTransport(PipedTransport.Builder())),
            *test(
                ProtocolMessenger.Builder()
                    .setTransport(Transport.Builder().setTransport(LoopbackTransport.Builder()))
            ),
            *test(ProtocolMessenger.Builder().setTransport(LoopbackTransport.Builder())),
            *test(LoopbackMessenger.Builder()),
            *test(
                Messenger.Builder().setMessenger(
                    ProtocolMessenger.Builder()
                        .setTransport(Transport.Builder().setTransport(PipedTransport.Builder()))
                )
            ),
            *test(
                Messenger.Builder().setMessenger(
                    ProtocolMessenger.Builder().setTransport(PipedTransport.Builder())
                )
            ),
            *test(
                Messenger.Builder().setMessenger(
                    ProtocolMessenger.Builder()
                        .setTransport(Transport.Builder().setTransport(LoopbackTransport.Builder()))
                )
            ),
            *test(
                Messenger.Builder().setMessenger(
                    ProtocolMessenger.Builder().setTransport(LoopbackTransport.Builder())
                )
            ),
            *test(Messenger.Builder().setMessenger(LoopbackMessenger.Builder())),
        )
    }

    @Before
    open fun assumeRunOnAndroid() = TestUtils.assumeAndroid()

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

        val transport = mProvider.build(client)

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