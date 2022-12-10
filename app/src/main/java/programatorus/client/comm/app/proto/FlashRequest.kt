package programatorus.client.comm.app.proto

import programatorus.client.comm.app.IRequester
import programatorus.client.model.Board
import programatorus.client.model.Firmware
import programus.proto.Protocol
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase

class FlashRequest(val board: Board, val firmware: Firmware) : IRequester<String> {

    override fun prepareRequest(): GenericMessage.Builder =
        GenericMessage.newBuilder()
            .setFlashRequest(
                Protocol.FlashRequest.newBuilder()
                    .setBoard(
                        Protocol.Board.newBuilder()
                            .setFavourite(board.isFavorite)
                            .setName(board.name)
                            .build()
                    )
                    .setFirmware(
                        Protocol.Firmware.newBuilder()
                            .setFavourite(firmware.isFavorite)
                            .setName(firmware.name)
                            .build()
                    )
                    .build()
            )

    override val responsePayloadCase = PayloadCase.FLASHRESPONSE

    override fun handleResponse(message: GenericMessage): String {
        return message.flashResponse.message
    }

}