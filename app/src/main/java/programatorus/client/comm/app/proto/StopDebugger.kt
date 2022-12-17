package programatorus.client.comm.app.proto

import programatorus.client.comm.app.IRequester
import programus.proto.Protocol.DebuggerStop
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase

class StopDebugger : IRequester<Unit> {

    override fun prepareRequest(): GenericMessage.Builder =
        GenericMessage.newBuilder()
            .setDebuggerStop(DebuggerStop.newBuilder())

    override val responsePayloadCase: PayloadCase
        get() = PayloadCase.OK

    override fun handleResponse(message: GenericMessage) {
    }

}