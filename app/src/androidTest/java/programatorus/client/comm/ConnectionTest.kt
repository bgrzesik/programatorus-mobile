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
import programatorus.client.comm.presentation.MessageLoopbackTest
import programatorus.client.comm.presentation.Messenger
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
public open class ConnectionTest(
    @Suppress("unused")
    private var mTestName: String,
    private var mProvider: (IConnectionClient) -> IConnection
) {

    companion object {
        private fun pt(v: (ITransportClient) -> ITransport): (IConnectionClient) -> ITransport =
            { v(object : ConnectionClientDelegate(it), ITransportClient {}) }

        private fun pm(v: (ITransportClient) -> ITransport): (IConnectionClient) -> Messenger =
            { Messenger(v, object : ConnectionClientDelegate(it), IMessageClient {}) }

        private fun m(v: (IMessageClient) -> IMessenger): (IConnectionClient) -> IMessenger =
            { v(object : ConnectionClientDelegate(it), IMessageClient {}) }

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun parameters(): Array<Array<Any>> = arrayOf(
            arrayOf("Transport(PipedTransport)", pt { Transport(::PipedTransport, it) }),
            arrayOf("PipedTransport", pt { PipedTransport(it) }),
            arrayOf("Transport(LoopbackTransport)", pt { Transport(::LoopbackTransport, it) }),
            arrayOf("LoopbackTransport", pt { LoopbackTransport(it) }),

            arrayOf("Messenger(Transport(PipedTransport))", pm { Transport(::PipedTransport, it) }),
            arrayOf("Messenger(PipedTransport)", pm { PipedTransport(it) }),
            arrayOf("Messenger(Transport(LoopbackTransport))", pm { Transport(::LoopbackTransport, it) }),
            arrayOf("Messenger(LoopbackTransport)", pm { LoopbackTransport(it) }),
            arrayOf("LoopbackMessenger", m { LoopbackMessenger(it) })
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