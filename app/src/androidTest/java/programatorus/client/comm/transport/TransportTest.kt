package programatorus.client.comm.transport

import org.hamcrest.CoreMatchers
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
open class TransportTest(
    @Suppress("unused")
    private var mTestName: String,
    private var mProvider: TransportProvider
) {

    companion object {
        private fun p(v: TransportProvider) = v

        @JvmStatic
        @Parameterized.Parameters(name="{0}")
        fun parameters(): Array<Array<Any>> = arrayOf(
            arrayOf("Transport(Piped)", p { Transport(::PipedTransport, it) }),
            arrayOf("Piped", p { PipedTransport(it) }),
            arrayOf("Transport(Loopback)", p { Transport(::LoopbackTransport, it) }),
            arrayOf("Loopback", p { LoopbackTransport(it) }),
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

        val client = object : ITransportClient {
            override fun onStateChanged(state: ConnectionState) {
                queue.add(state)
            }

            override fun onError() = Assert.fail("Transport Client caught error")
        }

        val transport = mProvider(client)

        var state = transport.state
        Assert.assertEquals(state, ConnectionState.DISCONNECTED)

        transport.reconnect()

        state = queue.poll(Long.MAX_VALUE, TimeUnit.DAYS)
        Assert.assertEquals(state, ConnectionState.CONNECTING)

        state = queue.poll(Long.MAX_VALUE, TimeUnit.DAYS)
        Assert.assertEquals(state, ConnectionState.CONNECTED)

        transport.disconnect()

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