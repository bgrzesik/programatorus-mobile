package programatorus.client.comm.transport

import android.util.Log
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.client.comm.TestUtils
import programatorus.client.comm.transport.io.PipedTransport
import programatorus.client.comm.transport.mock.LoopbackTransport
import programatorus.client.comm.transport.wrapper.Transport
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@RunWith(Parameterized::class)
open class PacketLoopbackTest(
    @Suppress("unused")
    private var mTestName: String,
    private var mProvider: ITransportProvider,
    private var mCount: Int
) {
    companion object {
        private const val TAG = "PacketLoopbackTest"

        private fun test(v: ITransportProvider): Array<Array<Any>> {
            val counts = arrayOf(0, 1, 10, 100)
            val name = TestUtils.getTransportName(v)
            return counts.map { arrayOf(name, v, it) }.toTypedArray()
        }

        @JvmStatic
        @Parameterized.Parameters(name = "{0} x {2}")
        @Suppress("NestedLambdaShadowedImplicitParameter")
        internal fun parameters(): Array<Array<Any>> = arrayOf(
            *test(Transport.Builder().setTransport(PipedTransport.Builder())),
            *test(PipedTransport.Builder()),
            *test(Transport.Builder().setTransport(LoopbackTransport.Builder())),
            *test(LoopbackTransport.Builder())
        )
    }

    @Before
    open fun assumeRunOnAndroid() = TestUtils.assumeAndroid()

    @Test(timeout = 8000)
    fun testLoopbackSendReceive() {
        val queue = LinkedBlockingQueue<ByteArray>()

        val client = object : ITransportClient {
            override fun onPacketReceived(packet: ByteArray) {
                Log.i(TAG, "onPacketReceived(): packet=${packet.decodeToString()}")
                queue.add(packet)
            }

            override fun onError() = Assert.fail("Transport Client caught error")
        }

        val transport = mProvider.build(client)
        transport.reconnect()

        val messages = mutableListOf<ByteArray>()
        for (i in 0 until mCount) {
            val message = "Test $i message".toByteArray()
            messages.add(message)
            transport.send(message)
            Log.i(TAG, "sent packet=${message.decodeToString()}")
        }

        for (message in messages) {
            val received = queue.poll(Long.MAX_VALUE, TimeUnit.DAYS)
            Assert.assertNotNull(received)
            Log.i(TAG, "dequeued packet=${received.decodeToString()}")
            Assert.assertArrayEquals(message, received)
        }

        Assert.assertNull(
            "Received more messages then expected",
            queue.poll(1000, TimeUnit.MILLISECONDS)
        )

        transport.disconnect()
    }
}