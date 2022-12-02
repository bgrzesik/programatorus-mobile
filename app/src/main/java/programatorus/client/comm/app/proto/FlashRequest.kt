package programatorus.client.comm.app.proto

import android.util.Log
import programatorus.client.comm.app.IRequester
import programatorus.client.model.Board
import programatorus.client.model.Firmware
import programus.proto.Protocol
import programus.proto.Protocol.FlashRequest.newBuilder
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase
import programus.proto.Protocol.GenericMessage.newBuilder

class FlashRequest(val board: Board, val firmware: Firmware) : IRequester<String> {

    override fun prepareRequest(): GenericMessage.Builder =
        Protocol.GenericMessage.newBuilder()
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
        Log.d("FLASH", "handleResponse: $message.flashResponse.message")
        return message.flashResponse.message
    }

}