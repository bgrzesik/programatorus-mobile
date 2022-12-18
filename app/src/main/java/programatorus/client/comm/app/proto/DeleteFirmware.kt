package programatorus.client.comm.app.proto

import programatorus.client.comm.app.IRequester
import programus.proto.Protocol.DeleteFile
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase

class DeleteFirmware(val name: String) : IRequester<Unit> {

    override fun prepareRequest(): GenericMessage.Builder =
        GenericMessage.newBuilder()
            .setDeleteFile(DeleteFile.newBuilder()
                    .setName(name))

    override val responsePayloadCase = PayloadCase.OK

    override fun handleResponse(message: GenericMessage) {
    }

}