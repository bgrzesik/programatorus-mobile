package programatorus.client.comm.app.proto

import programatorus.client.comm.app.IRequester
import programatorus.client.model.FirmwareData
import programus.proto.Protocol

class PutFirmware(val data:FirmwareData) : IRequester<Boolean> {

    override fun prepareRequest(): Protocol.GenericMessage.Builder =
        Protocol.GenericMessage.newBuilder()
            .setPutFirmwareRequest(Protocol.PutFirmwareRequest.newBuilder()
                .addAllAll(data.all.map {
                    Protocol.Firmware.newBuilder()
                        .setFavourite(it.isFavorite)
                        .setName(it.name)
                        .build()
                })
                .addAllFavorites(data.favorites.map {
                    Protocol.Firmware.newBuilder()
                        .setFavourite(it.isFavorite)
                        .setName(it.name)
                        .build()
                })
                .build())

    override val responsePayloadCase = Protocol.GenericMessage.PayloadCase.PUTFIRMWARERESPONSE

    override fun handleResponse(message: Protocol.GenericMessage): Boolean {
        return message.putFirmwareResponse.success
    }

}