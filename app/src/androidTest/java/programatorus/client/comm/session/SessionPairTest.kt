package programatorus.client.comm.session

import android.util.Log
import io.mockk.*
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.client.comm.TestUtils
import programatorus.client.comm.transport.ConnectionState
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.TestMessage
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(Parameterized::class)
open class SessionPairTest(
    private val mTestName: String,
    private val mWrapTransport: Boolean,
    private val mWrapMessenger: Boolean
) {
    companion object {
        const val TAG = "SessionPairTest"

        private fun test(wrapTransport: Boolean, wrapMessenger: Boolean): Array<Any> {
            val (left, _) = TestUtils.createSessionPair(wrapTransport, wrapMessenger)
            val name = TestUtils.getSessionName(left)
            return arrayOf(name, wrapTransport, wrapMessenger)
        }

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        internal fun parameters(): Array<Array<Any>> {
            return arrayOf(
                test(wrapTransport = false, wrapMessenger = false),
                test(wrapTransport = true, wrapMessenger = false),
                test(wrapTransport = false, wrapMessenger = true),
                test(wrapTransport = true, wrapMessenger = true),
            )
        }

    }

    private fun testPair(
        leftClient: ISessionClient,
        rightClient: ISessionClient,
        body: (ISession, ISession) -> Unit
    ) {
        val (leftBuilder, rightBuilder) = TestUtils.createSessionPair(
            mWrapTransport,
            mWrapMessenger
        )
        val left = leftBuilder.build(leftClient)
        val right = rightBuilder.build(rightClient)

        left.reconnect()
        right.reconnect()

        body(left, right)

        left.disconnect()
        right.disconnect()
    }

    @Test(timeout = 3000)
    fun testConnected() {
        val latch = CountDownLatch(2)

        val client = object : ISessionClient, SessionClientFailOnRequest {
            override fun onStateChanged(state: ConnectionState) {
                if (state == ConnectionState.CONNECTED) {
                    latch.countDown()
                }
            }
        }

        testPair(client, client) { _, _ ->
            latch.await()
        }
    }

    @Test(timeout = 10000)
    fun testKeepAliveDontReconnect() {
        val latch = CountDownLatch(2)
        val shouldBeConnected = AtomicBoolean(true)

        val client = object : ISessionClient, SessionClientFailOnRequest {
            override fun onStateChanged(state: ConnectionState) {
                if (state == ConnectionState.CONNECTED && latch.count > 0) {
                    latch.countDown()
                    return
                } else if (latch.count > 0) {
                    return
                }
                Assert.assertTrue(
                    "Connection should be kept alive properly",
                    shouldBeConnected.get()
                )
            }
        }

        testPair(client, client) { _, _ ->
            shouldBeConnected.set(true)
            latch.await()
            shouldBeConnected.set(false)
            // Keep session alive for 5 seconds in order to force keepAlive being send
            Thread.sleep(5000)
            shouldBeConnected.set(true)
        }
    }

    @Test(timeout = 3000)
    open fun testRequestResponse() {
        val exchangeLeft = "Hello there"
        val exchangeRight = "General Kenobi"
        val canDie = AtomicBoolean(false)

        val clientLeft = object : ISessionClient, SessionClientFailOnRequest {
            override fun onStateChanged(state: ConnectionState) {
                Log.i(TAG, "onStateChanged(): Left state=$state")
                if (!canDie.get() && (state == ConnectionState.DISCONNECTING || state == ConnectionState.DISCONNECTED)) {
                    Log.e(TAG, "Left end died unexpectedly")
                    Assert.fail("Left end died unexpectedly")
                }
            }
        }

        val clientRight = object : ISessionClient {

            override fun onRequest(request: GenericMessage): CompletableFuture<GenericMessage> {
                Log.d(TAG, "onRequest(): request=$request")
                Assert.assertEquals(request.payloadCase, GenericMessage.PayloadCase.TEST)
                Assert.assertEquals(request.test.value, exchangeLeft)

                return CompletableFuture<GenericMessage>().apply {
                    val response = GenericMessage.newBuilder()
                        .setTest(
                            TestMessage.newBuilder()
                                .setValue(exchangeRight)
                        )
                        .build()

                    Log.d(TAG, "onRequest(): response=$response")

                    complete(response)
                }
            }

            override fun onStateChanged(state: ConnectionState) {
                Log.i(TAG, "onStateChanged(): Right state=$state")
                if (!canDie.get() && (state == ConnectionState.DISCONNECTING || state == ConnectionState.DISCONNECTED)) {
                    Log.e(TAG, "Right end died unexpectedly")
                    Assert.fail("Right end died unexpectedly")
                }
            }
        }

        testPair(clientLeft, clientRight) { left, _ ->
            val request = GenericMessage.newBuilder()
                .setTest(
                    TestMessage.newBuilder()
                        .setValue(exchangeLeft)
                )
                .build()

            val response = left.request(request).get()
            Assert.assertEquals(response.payloadCase, GenericMessage.PayloadCase.TEST)
            Assert.assertEquals(response.test.value, exchangeRight)
            canDie.set(true)
        }
    }
}