package programatorus.client.comm.app.proto

import programatorus.client.comm.app.IRequester
import programatorus.client.model.Board
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase
import programus.proto.Protocol.GetBoardsRequest

class GetBoards : IRequester<List<Board>> {

    override fun prepareRequest(): GenericMessage.Builder =
        GenericMessage.newBuilder()
            .setGetBoardsRequest(GetBoardsRequest.newBuilder())

    override val responsePayloadCase = PayloadCase.GETBOARDSRESPONSE

    override fun handleResponse(message: GenericMessage): List<Board> =
            message.getBoardsResponse.boardList.map {
                Board(it.name, it.favourite)
            }

}