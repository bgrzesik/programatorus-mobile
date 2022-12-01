package programatorus.client.comm.app.proto

import programatorus.client.comm.app.IRequester
import programatorus.client.model.Firmware
import programatorus.client.model.FirmwareData
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase
import programus.proto.Protocol.GetFirmwareRequest

class GetFirmware : IRequester<FirmwareData> {

    override fun prepareRequest(): GenericMessage.Builder =
        GenericMessage.newBuilder()
            .setGetFirmwareRequest(GetFirmwareRequest.newBuilder())

    override val responsePayloadCase = PayloadCase.GETFIRMWARERESPONSE

    override fun handleResponse(message: GenericMessage): FirmwareData {
        val favorites = message.getFirmwareResponse.favoritesList.map {
            Firmware(it.name, it.favourite)
        }
        val all = message.getFirmwareResponse.allList.map {
            Firmware(it.name, it.favourite)
        }
        return FirmwareData(all, favorites)
    }

}