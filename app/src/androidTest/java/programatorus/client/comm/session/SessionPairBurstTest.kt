package programatorus.client.comm.session

import io.mockk.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.client.comm.TestUtils
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.TestMessage
import java.util.concurrent.CompletableFuture

@RunWith(Parameterized::class)
open class SessionPairBurstTest(
    private val mTestName: String,
    private val mWrapTransport: Boolean,
    private val mWrapMessenger: Boolean,
    private val mCount: Int
) {
    companion object {
        const val TAG = "SessionPairBurstTest"

        private fun test(wrapTransport: Boolean, wrapMessenger: Boolean): Array<Array<Any>> {
            val counts = arrayOf(0, 1, 10, 100)
            val (left, _) = TestUtils.createSessionPair(wrapTransport, wrapMessenger)
            val name = TestUtils.getSessionName(left)

            return counts.map { arrayOf<Any>(name, wrapTransport, wrapMessenger, it) }
                .toTypedArray()
        }

        @JvmStatic
        @Parameterized.Parameters(name = "{0} x {3}")
        internal fun parameters(): Array<Array<Any>> {
            return arrayOf(
                *test(wrapTransport = false, wrapMessenger = false),
                *test(wrapTransport = true, wrapMessenger = false),
                *test(wrapTransport = false, wrapMessenger = true),
                *test(wrapTransport = true, wrapMessenger = true),
            )
        }

    }

    @Before
    open fun assumeRunOnAndroid() = TestUtils.assumeAndroid()

    @Test(timeout=2000)
    fun testRequestResponse() {
        val (leftBuilder, rightBuilder) = TestUtils.createSessionPair(
            mWrapTransport,
            mWrapMessenger
        )

        val leftClient = object : ISessionClient, SessionClientFailOnRequest {
        }

        val rightClient = object : ISessionClient {
            override fun onRequest(request: GenericMessage): CompletableFuture<GenericMessage> {
                Assert.assertEquals(request.payloadCase, GenericMessage.PayloadCase.TEST)
                Assert.assertEquals(request.test.value, "Ping")

                val response = GenericMessage.newBuilder()
                    .setTest(
                        TestMessage.newBuilder()
                            .setValue("Pong")
                    )
                    .build()

                return CompletableFuture.completedFuture(response)
            }
        }


        val left = leftBuilder.build(leftClient)
        val right = rightBuilder.build(rightClient)

        left.reconnect()
        right.reconnect()

        val requests = mutableListOf<CompletableFuture<GenericMessage>>()
        for (i in 0 until mCount) {
            val request = GenericMessage.newBuilder()
                .setTest(
                    TestMessage.newBuilder()
                        .setValue("Ping")
                )
                .build()

            requests.add(left.request(request))
        }

        for ((i, request) in requests.withIndex()) {
            val response = request.get()
            Assert.assertEquals(response.payloadCase, GenericMessage.PayloadCase.TEST)
            Assert.assertEquals(response.test.value, "Pong")
        }

        left.disconnect()
        right.disconnect()
    }

    @Test(timeout=2000)
    fun testOrder() {
        val (leftBuilder, rightBuilder) = TestUtils.createSessionPair(
            mWrapTransport,
            mWrapMessenger
        )

        val leftClient = object : ISessionClient, SessionClientFailOnRequest {
        }

        val rightClient = object : ISessionClient {
            var i = 0

            override fun onRequest(request: GenericMessage): CompletableFuture<GenericMessage> {
                Assert.assertEquals(request.payloadCase, GenericMessage.PayloadCase.TEST)
                Assert.assertEquals(request.test.value, "Ping $i")

                val response = GenericMessage.newBuilder()
                    .setTest(
                        TestMessage.newBuilder()
                            .setValue("Pong $i")
                    )
                    .build()

                i++

                return CompletableFuture.completedFuture(response)
            }
        }


        val left = leftBuilder.build(leftClient)
        val right = rightBuilder.build(rightClient)

        left.reconnect()
        right.reconnect()

        val requests = mutableListOf<CompletableFuture<GenericMessage>>()
        for (i in 0 until mCount) {
            val request = GenericMessage.newBuilder()
                .setTest(
                    TestMessage.newBuilder()
                        .setValue("Ping $i")
                )
                .build()

            requests.add(left.request(request))
        }

        for ((i, request) in requests.withIndex()) {
            val response = request.get()
            Assert.assertEquals(response.payloadCase, GenericMessage.PayloadCase.TEST)
            Assert.assertEquals(response.test.value, "Pong $i")
        }

        left.disconnect()
        right.disconnect()
    }

}