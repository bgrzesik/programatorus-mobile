package programatorus.client.comm.app.proto

import programatorus.client.comm.app.IRequester
import programatorus.client.model.Board
import programatorus.client.model.BoardsData
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase
import programus.proto.Protocol.GetBoardsRequest

class GetBoards : IRequester<BoardsData> {

    override fun prepareRequest(): GenericMessage.Builder =
        GenericMessage.newBuilder()
            .setGetBoardsRequest(GetBoardsRequest.newBuilder())

    override val responsePayloadCase = PayloadCase.GETBOARDSRESPONSE

    override fun handleResponse(message: GenericMessage): BoardsData {
        val favorites = message.getBoardsResponse.favoritesList.map {
            Board(it.name, it.favourite)
        }
        val all = message.getBoardsResponse.allList.map {
            Board(it.name, it.favourite)
        }
        return BoardsData(all, favorites)
    }

}