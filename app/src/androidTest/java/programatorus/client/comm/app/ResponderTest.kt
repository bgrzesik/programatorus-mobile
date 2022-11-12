package programatorus.client.comm.app

import com.google.protobuf.Empty
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import programatorus.client.comm.IConnectionClient
import programatorus.client.comm.TestUtils
import programatorus.client.comm.app.proto.OnDeviceStatusUpdate
import programatorus.client.comm.app.proto.OnDeviceStatusUpdate.DeviceStatus
import programatorus.client.comm.presentation.mock.IMockMessengerEndpoint
import programatorus.client.comm.transport.ConnectionState
import programus.proto.Protocol.DeviceUpdateStatus
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch

open class ResponderTest {

    @Before
    open fun assumeRunOnAndroid() = TestUtils.assumeAndroid()

    @Test(timeout=1000)
    fun testDeviceUpdateStatus() {
        val sendLatch = CountDownLatch(1)
        val latch = CountDownLatch(2)

        val onDeviceStatusUpdate = object : OnDeviceStatusUpdate {
            override fun onRequest(request: DeviceStatus): CompletableFuture<Unit> {
                latch.countDown()
                Assert.assertEquals(request.status, DeviceStatus.Status.FLASHING)
                Assert.assertTrue(request.flashingProgress.isPresent)
                Assert.assertEquals(request.flashingProgress.get(), 0.5f)
                Assert.assertTrue(request.image.isPresent)
                Assert.assertEquals(request.image.get(), "mona_lisa.bin")
                return CompletableFuture.completedFuture(Unit)
            }
        }

        val connectionClient = object : IConnectionClient {
            override fun onStateChanged(state: ConnectionState) {
                if (state == ConnectionState.CONNECTED) {
                    sendLatch.countDown()
                }
            }

            override fun onError() = Assert.fail()
        }

        val router = RequestRouter(listOf(onDeviceStatusUpdate), connectionClient)
        val endpoint = object : IMockMessengerEndpoint {
            override fun onMessage(packet: GenericMessage): GenericMessage? {
                val builder = when (packet.payloadCase) {
                    PayloadCase.OK -> {
                        latch.countDown()
                        Assert.assertEquals(packet.response, 2137L)
                        return null
                    }

                    PayloadCase.HEARTBEAT ->
                        GenericMessage.newBuilder()
                            .setResponse(packet.request)
                            .setOk(Empty.newBuilder())

                    else -> {
                        Assert.fail("Unexpected message")
                        return null
                    }
                }

                return builder.build()
            }
        }

        val (session, orchestrator) = TestUtils.createMockedSession(router, endpoint)

        session.reconnect()

        sendLatch.await()
        orchestrator.mockMessage(
            GenericMessage.newBuilder()
                .setRequest(2137L)
                .setDeviceUpdateStatus(
                    DeviceUpdateStatus.newBuilder()
                        .setStatus(DeviceUpdateStatus.Status.FLASHING)
                        .setFlashingProgress(0.5f)
                        .setImage("mona_lisa.bin")
                )
                .build()
        )

        latch.await()
        session.disconnect()
    }

}