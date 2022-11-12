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

    @Test
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
                                    .addName("test 0")
                                    .addName("test 1")
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
        Assert.assertArrayEquals(boards.boards, arrayOf("test 0", "test 1"))

        session.disconnect()
    }

}