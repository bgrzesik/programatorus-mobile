package programatorus.client.comm

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.client.comm.presentation.*
import programatorus.client.comm.presentation.mock.LoopbackMessenger
import programatorus.client.comm.transport.*
import programatorus.client.comm.transport.io.PipedTransport
import programatorus.client.comm.transport.mock.LoopbackTransport
import programatorus.client.comm.transport.wrapper.Transport
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@RunWith(Parameterized::class)
open class ConnectionTest(
    @Suppress("unused")
    private var mTestName: String,
    private var mProvider: (IConnectionClient) -> IConnection
) {

    companion object {
        private fun test(t: ITransportProvider): Array<Any> =
            arrayOf(TestUtils.getTransportName(t), { client: IConnectionClient ->
                t.build(object : ConnectionClientDelegate(client), ITransportClient {})
            })

        private fun test(m: IMessengerProvider): Array<Any> =
            arrayOf(TestUtils.getMessengerName(m), { client: IConnectionClient ->
                m.build(object : ConnectionClientDelegate(client), IMessageClient {})
            })

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        @Suppress("NestedLambdaShadowedImplicitParameter")
        fun parameters(): Array<Array<Any>> = arrayOf(
            test(Transport.Builder().setTransport(PipedTransport.Builder())),
            test(PipedTransport.Builder()),
            test(Transport.Builder().setTransport(LoopbackTransport.Builder())),
            test(LoopbackTransport.Builder()),
            test(
                ProtocolMessenger.Builder()
                    .setTransport(Transport.Builder().setTransport(PipedTransport.Builder()))
            ),
            test(
                ProtocolMessenger.Builder().setTransport(PipedTransport.Builder())
            ),
            test(
                ProtocolMessenger.Builder()
                    .setTransport(Transport.Builder().setTransport(LoopbackTransport.Builder()))
            ),
            test(
                ProtocolMessenger.Builder().setTransport(LoopbackTransport.Builder())
            ),
            test(LoopbackMessenger.Builder()),
            test(
                Messenger.Builder().setMessenger(
                    ProtocolMessenger.Builder()
                        .setTransport(Transport.Builder().setTransport(PipedTransport.Builder()))
                )
            ),
            test(
                Messenger.Builder().setMessenger(
                    ProtocolMessenger.Builder().setTransport(PipedTransport.Builder())
                )
            ),
            test(
                Messenger.Builder().setMessenger(
                    ProtocolMessenger.Builder()
                        .setTransport(Transport.Builder().setTransport(LoopbackTransport.Builder()))
                )
            ),
            test(
                Messenger.Builder().setMessenger(
                    ProtocolMessenger.Builder().setTransport(LoopbackTransport.Builder())
                )
            ),
            test(
                Messenger.Builder().setMessenger(LoopbackMessenger.Builder())
            ),
        )
    }

    @Before
    open fun assumeRunOnAndroid() = TestUtils.assumeAndroid()

    @Test(timeout = 5000)
    fun testStateMachine() {
        val queue = LinkedBlockingQueue<ConnectionState>()

        val client = object : IConnectionClient {
            override fun onStateChanged(state: ConnectionState) {
                queue.add(state)
            }

            override fun onError() = Assert.fail("Transport Client caught error")
        }

        val conn: IConnection = mProvider(client)

        var state = conn.state
        Assert.assertEquals(state, ConnectionState.DISCONNECTED)

        conn.reconnect()

        state = queue.poll(Long.MAX_VALUE, TimeUnit.DAYS)
        Assert.assertEquals(state, ConnectionState.CONNECTING)

        state = queue.poll(Long.MAX_VALUE, TimeUnit.DAYS)
        Assert.assertEquals(state, ConnectionState.CONNECTED)

        conn.disconnect()

        state = queue.poll(Long.MAX_VALUE, TimeUnit.DAYS)
        Assert.assertEquals(state, ConnectionState.DISCONNECTING)

        state = queue.poll(Long.MAX_VALUE, TimeUnit.DAYS)
        Assert.assertEquals(state, ConnectionState.DISCONNECTED)

        Assert.assertNull(
            "The programatorus.local.transport should not change state",
            queue.poll(1000, TimeUnit.MILLISECONDS)
        )
    }

}