package programatorus.client.comm.app.proto

import programatorus.client.comm.app.IRequester
import programus.proto.Protocol.DebuggerLine
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase

class SendDebuggerLine(
    val line: String
) : IRequester<Unit> {

    override fun prepareRequest(): GenericMessage.Builder =
        GenericMessage.newBuilder()
            .setDebuggerLine(DebuggerLine.newBuilder()
                .setLine(line))

    override val responsePayloadCase: PayloadCase
        get() = PayloadCase.OK

    override fun handleResponse(message: GenericMessage) {
    }

}