package programatorus.client.comm.app

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.client.comm.TestUtils
import programatorus.client.comm.app.proto.GetBoards
import programatorus.client.comm.app.proto.OnDeviceStatusUpdate
import programus.proto.Protocol.*
import programus.proto.Protocol.GenericMessage.PayloadCase
import programatorus.client.comm.app.proto.FileUpload
import programatorus.client.model.Board
import programatorus.client.model.BoardsData
import programus.proto.Protocol
import programus.proto.Protocol.FileUpload.EventCase
import java.io.ByteArrayInputStream
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

    @Test(timeout=2000)
    fun testDeviceStatusUpdate() {
        val latch = CountDownLatch(2)
        val deviceStatusUpdate = object : IRequester<Unit> {
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
            listOf(onDeviceStatusUpdate),
            mWrapTransport, mWrapMessenger
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

            override fun onRequest(message: GenericMessage): CompletableFuture<GenericMessage> {
                val data = BoardsData(
                    (0..499).map {
                        Board("test $it", it%2 == 0)
                    }
                    ,
                    listOf(
                        Board("test 0", true)
                    )
                )
                return CompletableFuture.completedFuture(
                    GenericMessage.newBuilder()
                        .setGetBoardsResponse(
                            GetBoardsResponse.newBuilder()
                                .addAllAll(data.all.map {
                                    Protocol.Board.newBuilder()
                                        .setFavourite(it.isFavorite)
                                        .setName(it.name)
                                        .build()
                                })
                                .addAllFavorites(data.favorites.map {
                                    Protocol.Board.newBuilder()
                                        .setFavourite(it.isFavorite)
                                        .setName(it.name)
                                        .build()
                                })
                        ).build()
                )
            }

        }

        val (left, right) = TestUtils.createSessionResponderPair(
            emptyList(),
            listOf(onGetBoardsResponder),
            mWrapTransport, mWrapMessenger
        )

        left.reconnect()
        right.reconnect()

        val boards = GetBoards().request(left).get()
        Assert.assertEquals(boards.all.size, 500)
        Assert.assertEquals(boards.favorites.size, 1)
        Assert.assertEquals(boards.all[0].name, "test 0")
        Assert.assertTrue(boards.all[0].isFavorite)
        Assert.assertEquals(boards.all[1].name, "test 1")
        Assert.assertFalse(boards.all[1].isFavorite)

        left.disconnect()
        right.disconnect()
    }


    @Test
    fun testFileUpload() {
        val fileName = "test_file.bin"
        val fileBytes = ByteArray(8192) { 0xaa.toByte() }

        val onFileUploadResponder = object : IResponder {
            var mExpectedIndex: Int = 0
            var mBuffer = mutableListOf<Byte>()

            override val requestPayloadCase: PayloadCase
                get() = PayloadCase.FILEUPLOAD

            fun handleStart(start: Protocol.FileUpload.Start) {
                Assert.assertEquals(0, mBuffer.size)
                Assert.assertEquals(fileName, start.name)
                Assert.assertEquals(fileBytes.size, start.size.toInt())
            }

            fun handlePart(part: Protocol.FileUpload.Part) {
                Assert.assertEquals(mExpectedIndex, part.partNo)

                mExpectedIndex++
                mBuffer.addAll(part.chunk)

                Assert.assertTrue(mBuffer.size <= fileBytes.size)
            }

            fun handleFinish(_finish: Protocol.FileUpload.Finish) {
                Assert.assertArrayEquals(fileBytes, mBuffer.toByteArray())
            }

            override fun onRequest(message: GenericMessage): CompletableFuture<GenericMessage> {
                val fileUpload = message.fileUpload

                if (fileUpload.eventCase != EventCase.START) {
                    Assert.assertEquals(102, fileUpload.uid)
                }

                when (fileUpload.eventCase) {
                    EventCase.START -> handleStart(fileUpload.start)
                    EventCase.PART -> handlePart(fileUpload.part)
                    EventCase.FINISH -> handleFinish(fileUpload.finish)
                    else -> {
                        Assert.fail()
                        throw RuntimeException()
                    }
                }

                return CompletableFuture.completedFuture(
                    GenericMessage.newBuilder()
                        .setFileUpload(Protocol.FileUpload.newBuilder()
                            .setUid(102)
                            .setResult(Protocol.FileUpload.Result.OK))
                        .build()
                )
            }
        }

        val (left, right) = TestUtils.createSessionResponderPair(
            emptyList(),
            listOf(onFileUploadResponder),
            mWrapTransport, mWrapMessenger
        )

        left.reconnect()
        right.reconnect()

        FileUpload.upload(left, fileName, fileBytes.size, ByteArrayInputStream(fileBytes)).get()

        left.disconnect()
        right.disconnect()
    }

}