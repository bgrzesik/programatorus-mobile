package programatorus.client.comm.app

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.client.comm.TestUtils
import programatorus.client.comm.app.proto.GetBoards
import programatorus.client.comm.app.proto.OnDeviceStatusUpdate
import programus.proto.Protocol.*
import programus.proto.Protocol.GenericMessage.PayloadCase
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch

@RunWith(Parameterized::class)
open class RequesterResponderTest(
    private val mTestName: String,
    private val mWrapTransport: Boolean,
    private val mWrapMessenger: Boolean
) {
    companion object {
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

    @Before
    open fun assumeRunOnAndroid() = TestUtils.assumeAndroid()

    @Test(timeout=2000)
    fun testDeviceStatusUpdate() {
        val latch = CountDownLatch(2)
        val deviceStatusUpdate = object : Requester<Unit> {
            override fun prepareRequest() =
                GenericMessage.newBuilder()
                    .setDeviceUpdateStatus(
                        DeviceUpdateStatus.newBuilder()
                            .setStatus(DeviceUpdateStatus.Status.FLASHING)
                            .setFlashingProgress(0.5f)
                            .setImage("mona_lisa.bin")
                    )

            override val responsePayloadCase: PayloadCase
                get() = PayloadCase.OK

            override fun handleResponse(message: GenericMessage) {
                latch.countDown()
            }
        }


        val onDeviceStatusUpdate = object : OnDeviceStatusUpdate {
            override fun onRequest(request: OnDeviceStatusUpdate.DeviceStatus): CompletableFuture<Unit> {
                Assert.assertEquals(
                    request.status,
                    OnDeviceStatusUpdate.DeviceStatus.Status.FLASHING
                )
                Assert.assertTrue(request.flashingProgress.isPresent)
                Assert.assertEquals(request.flashingProgress.get(), 0.5f)
                Assert.assertTrue(request.image.isPresent)
                Assert.assertEquals(request.image.get(), "mona_lisa.bin")
                latch.countDown()
                return CompletableFuture.completedFuture(Unit)
            }
        }

        val (left, right) = TestUtils.createSessionResponderPair(
            emptyList(),
            listOf(onDeviceStatusUpdate)
        )

        left.reconnect()
        right.reconnect()

        deviceStatusUpdate.request(left)
        latch.await()

        left.disconnect()
        right.disconnect()
    }


    @Test(timeout=2000)
    fun testGetBoards() {
        val onGetBoardsResponder = object : IResponder {
            override val requestPayloadCase: PayloadCase
                get() = PayloadCase.GETBOARDSREQUEST

            override fun onRequest(message: GenericMessage): CompletableFuture<GenericMessage> =
                CompletableFuture.completedFuture(
                    GenericMessage.newBuilder()
                        .setGetBoardsResponse(
                            GetBoardsResponse.newBuilder()
                                .addName("test 0")
                                .addName("test 1")
                        )
                        .build()
                )

        }

        val (left, right) = TestUtils.createSessionResponderPair(
            emptyList(),
            listOf(onGetBoardsResponder)
        )

        left.reconnect()
        right.reconnect()

        val boards = GetBoards().request(left).get()
        Assert.assertArrayEquals(boards.boards, arrayOf("test 0", "test 1"))

        left.disconnect()
        right.disconnect()
    }

}