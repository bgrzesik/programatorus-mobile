package programatorus.client.comm.app.proto

import programatorus.client.comm.app.IRequester
import programatorus.client.model.BoardsData
import programus.proto.Protocol
import programus.proto.Protocol.GenericMessage

class PutBoards(val data: BoardsData) : IRequester<Boolean> {

    override fun prepareRequest(): GenericMessage.Builder =
        GenericMessage.newBuilder()
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

    override val responsePayloadCase = GenericMessage.PayloadCase.PUTBOARDSRESPONSE

    override fun handleResponse(message: GenericMessage): Boolean {
        return message.putBoardsResponse.success
    }

}