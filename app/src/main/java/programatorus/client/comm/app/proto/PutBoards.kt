package programatorus.client.comm.app.proto

import programatorus.client.comm.app.IRequester
import programatorus.client.model.BoardsData
import programus.proto.Protocol

class PutBoards(val data: BoardsData) : IRequester<Boolean> {

    override fun prepareRequest(): Protocol.GenericMessage.Builder =
        Protocol.GenericMessage.newBuilder()
            .setPutBoardsRequest(Protocol.PutBoardsRequest.newBuilder()
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
                .build())

    override val responsePayloadCase = Protocol.GenericMessage.PayloadCase.PUTBOARDSRESPONSE

    override fun handleResponse(message: Protocol.GenericMessage): Boolean {
        return message.putBoardsResponse.success
    }

}