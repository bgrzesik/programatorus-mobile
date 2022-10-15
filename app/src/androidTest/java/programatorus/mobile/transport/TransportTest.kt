package programatorus.mobile.transport

import org.junit.Assert
import org.junit.Test
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.comm.transport.ITransportClient
import programatorus.client.comm.transport.io.PipedTransport
import programatorus.client.comm.transport.wrapper.Transport
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

internal class TransportTest {

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
        val queue = LinkedBlockingQueue<ByteArray>()

        val client = object : ITransportClient {
            override fun onPacketReceived(packet: ByteArray) {
                println(packet.decodeToString())
                queue.add(packet)
            }

            override fun onError() = Assert.fail()
        }

        val transport = Transport(::PipedTransport, client)

        val messages = mutableListOf<ByteArray>()
        for (i in 0..num) {
            val message = "Test $i message".toByteArray()
            messages.add(message)
            transport.send(message)
        }

        for (message in messages) {
//            transport.send(message)

            val received = queue.poll(6000, TimeUnit.MILLISECONDS)

            Assert.assertNotNull(received)

            println(message.decodeToString() + " = " + received.decodeToString())

            Assert.assertArrayEquals(message, received)
        }

        Assert.assertNull("Received more messages then expected",
            queue.poll(1000, TimeUnit.MILLISECONDS))

        transport.disconnect()
    }

    @Test
    fun testStateMachine() {
        val queue = LinkedBlockingQueue<ConnectionState>()

        val client = object : ITransportClient {
            override fun onStateChanged(state: ConnectionState) {
                queue.add(state)
            }

            override fun onError() = Assert.fail()
        }

        val transport = Transport(::PipedTransport, client)

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

        Assert.assertNull("The transport should not change state",
            queue.poll(1000, TimeUnit.MILLISECONDS))
    }

}