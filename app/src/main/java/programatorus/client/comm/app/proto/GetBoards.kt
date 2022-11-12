package programatorus.client.comm.app.proto

import programatorus.client.comm.app.Requester
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase
import programus.proto.Protocol.GetBoardsRequest

class GetBoards : Requester<GetBoards.Boards> {

    override fun prepareRequest(): GenericMessage.Builder =
        GenericMessage.newBuilder()
            .setGetBoardsRequest(GetBoardsRequest.newBuilder())

    override val responsePayloadCase = PayloadCase.GETBOARDSRESPONSE

    override fun handleResponse(message: GenericMessage): Boards {
        val response = message.getBoardsResponse
        return Boards(response.nameList.toTypedArray())
    }

    data class Boards(
        val boards: Array<String>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Boards

            if (!boards.contentEquals(other.boards)) return false

            return true
        }

        override fun hashCode(): Int {
            return boards.contentHashCode()
        }
    }

}