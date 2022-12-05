package programatorus.client.comm.app

import com.google.protobuf.Empty
import org.junit.Assert
import org.junit.Test
import programatorus.client.comm.TestUtils
import programatorus.client.comm.app.proto.GetBoards
import programatorus.client.comm.presentation.mock.IMockMessengerEndpoint
import programatorus.client.model.Board
import programatorus.client.model.BoardsData
import programus.proto.Protocol
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase
import programus.proto.Protocol.GetBoardsResponse
import java.util.concurrent.CompletableFuture

open class RequesterTest {

    @Test(timeout=1000)
    fun testGetBoardsRequest() {
        val router = RequestRouter(emptyList())
        val endpoint = object : IMockMessengerEndpoint {
            override fun onMessage(packet: GenericMessage): GenericMessage? {
                val data = BoardsData(
                        listOf(
                                Board("Test 0", true),
                                Board("Test 1", false)
                        ),
                        listOf(
                                Board("Test 0", true)
                        )
                )

                val builder = when (packet.payloadCase) {
                    PayloadCase.GETBOARDSREQUEST ->
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
        Assert.assertEquals(boards.all.size, 2)
        Assert.assertEquals(boards.favorites.size, 1)
        Assert.assertEquals(boards.all[0].name, "test 0")
        Assert.assertTrue(boards.all[0].isFavorite)
        Assert.assertEquals(boards.all[1].name, "test 1")
        Assert.assertFalse(boards.all[1].isFavorite)

        session.disconnect()
    }

}