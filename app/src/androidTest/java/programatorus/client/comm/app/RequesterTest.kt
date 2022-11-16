package programatorus.client.comm.app

import com.google.protobuf.Empty
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import programatorus.client.comm.TestUtils
import programatorus.client.comm.app.proto.GetBoards
import programatorus.client.comm.presentation.mock.IMockMessengerEndpoint
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase
import programus.proto.Protocol.GetBoardsResponse

open class RequesterTest {

    @Before
    open fun assumeRunOnAndroid() = TestUtils.assumeAndroid()

    @Test(timeout=1000)
    fun testGetBoardsRequest() {
        val router = RequestRouter(emptyList())
        val endpoint = object : IMockMessengerEndpoint {
            override fun onMessage(packet: GenericMessage): GenericMessage? {
                val builder = when (packet.payloadCase) {
                    PayloadCase.GETBOARDSREQUEST ->
                        GenericMessage.newBuilder()
                            .setResponse(packet.request)
                            .setGetBoardsResponse(
                                GetBoardsResponse.newBuilder()
                                    .addBoard(GetBoardsResponse.Board.newBuilder()
                                        .setName("test 0")
                                        .setFavourite(true))
                                    .addBoard(GetBoardsResponse.Board.newBuilder()
                                        .setName("test 1")
                                        .setFavourite(false))
                            )

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

        val (session, _) = TestUtils.createMockedSession(router, endpoint)
        session.reconnect()

        val boards = GetBoards().request(session).get()
        Assert.assertEquals(boards.size, 2)
        Assert.assertEquals(boards[0].name, "test 0")
        Assert.assertTrue(boards[0].isFavorite)
        Assert.assertEquals(boards[1].name, "test 1")
        Assert.assertFalse(boards[1].isFavorite)

        session.disconnect()
    }

}