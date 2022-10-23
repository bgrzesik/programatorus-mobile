package programatorus.client.comm

import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.client.comm.presentation.IMessageClient
import programatorus.client.comm.presentation.IMessenger
import programatorus.client.comm.presentation.Messenger
import programatorus.client.comm.presentation.ProtocolMessenger
import programatorus.client.comm.presentation.mock.LoopbackMessenger
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.comm.transport.ITransport
import programatorus.client.comm.transport.ITransportClient
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
        private fun t(v: (ITransportClient) -> ITransport): Array<Any> {
            return arrayOf(TestUtils.getTransportName(v), { client: IConnectionClient ->
                v(object : ConnectionClientDelegate(client), ITransportClient {})
            })
        }

        private fun m(v: (IMessageClient) -> IMessenger): Array<Any> {
            return arrayOf(TestUtils.getMessengerName(v), { client: IConnectionClient ->
                v(object : ConnectionClientDelegate(client), IMessageClient {})
            })
        }

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        @Suppress("NestedLambdaShadowedImplicitParameter")
        fun parameters(): Array<Array<Any>> = arrayOf(
            // @formatter:off
            t { Transport(::PipedTransport, it) },
            t { PipedTransport(it) },
            t { Transport(::LoopbackTransport, it) },
            t { LoopbackTransport(it) },

            m { ProtocolMessenger({ Transport(::PipedTransport, it) }, it) },
            m { ProtocolMessenger({ PipedTransport(it) }, it) },
            m { ProtocolMessenger({ Transport(::LoopbackTransport, it) }, it) },
            m { ProtocolMessenger({ LoopbackTransport(it) }, it) },
            m { LoopbackMessenger(it) },

            m { Messenger({ ProtocolMessenger({ Transport(::PipedTransport, it) }, it) }, it) },
            m { Messenger({ ProtocolMessenger({ PipedTransport(it) }, it) }, it) },
            m { Messenger({ ProtocolMessenger({ Transport(::LoopbackTransport, it) }, it) }, it) },
            m { Messenger({ ProtocolMessenger({ LoopbackTransport(it) }, it) }, it) },
            m { Messenger({ LoopbackMessenger(it) }, it) },
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