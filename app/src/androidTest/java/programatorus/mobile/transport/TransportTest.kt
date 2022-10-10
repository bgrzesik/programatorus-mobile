package programatorus.mobile.transport

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import programatorus.client.transport.ConnectionState
import programatorus.client.transport.ITransportClient
import programatorus.client.transport.io.LoopbackTransport
import programatorus.client.transport.wrapper.Transport
import programus.proto.GenericMessage
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

internal class TransportTest {

    @Before
    fun mockLog() = TestUtils.mockLog()

    @Test
    fun testLoopbackSendReceive1() {
        testLoopbackSendReceive(1)
    }

    @Test
    fun testLoopbackSendReceive10() {
        testLoopbackSendReceive(10)
    }

    @Test
    fun testLoopbackSendReceive100() {
        testLoopbackSendReceive(100)
    }

    private fun testLoopbackSendReceive(num: Int) {
        val queue = ArrayBlockingQueue<GenericMessage>(1)

        val client = object : ITransportClient {
            override fun onMessageReceived(message: GenericMessage) {
                queue.add(message)
            }

            override fun onError() = Assert.fail()
        }

        val transport = Transport({ client, executor -> LoopbackTransport(client, executor) }, client)

        val messages = mutableListOf<GenericMessage>()
        for (i in 0..num) {
            messages.add(TestUtils.newTestMessage())
        }

        for (message in messages) {
            transport.send(message)

            val received = queue.poll(100, TimeUnit.MILLISECONDS)

            Assert.assertNotNull(received)
            Assert.assertEquals(message, received)
        }

        transport.disconnect()
    }

    @Test
    fun testStateMachine() {
        val queue = ArrayBlockingQueue<ConnectionState>(1)

        val client = object : ITransportClient {
            override fun onStateChanged(state: ConnectionState) {
                queue.add(state)
            }

            override fun onError() = Assert.fail()
        }

        val transport = Transport(::LoopbackTransport, client)

        var state = queue.poll(100, TimeUnit.MILLISECONDS)
        Assert.assertEquals(state, ConnectionState.DISCONNECTED)

        transport.reconnect()

        state = queue.poll(100, TimeUnit.MILLISECONDS)
        Assert.assertEquals(state, ConnectionState.CONNECTING)

        state = queue.poll(100, TimeUnit.MILLISECONDS)
        Assert.assertEquals(state, ConnectionState.CONNECTED)

        transport.disconnect()

        state = queue.poll(100, TimeUnit.MILLISECONDS)
        Assert.assertEquals(state, ConnectionState.DISCONNECTING)

        state = queue.poll(100, TimeUnit.MILLISECONDS)
        Assert.assertEquals(state, ConnectionState.DISCONNECTED)
    }

}