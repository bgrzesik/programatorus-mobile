package programatorus.client.comm.app.proto

import programatorus.client.comm.app.IRequester
import programus.proto.Protocol.DebuggerStart
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase

class StartDebugger(
    val firmware: String,
    val target: String,
) : IRequester<Int> {

    override fun prepareRequest(): GenericMessage.Builder =
        GenericMessage.newBuilder()
            .setDebuggerStart(
                DebuggerStart.newBuilder()
                    .setFirmware(firmware)
                    .setTarget(target)
            )

    override val responsePayloadCase: PayloadCase
        get() = PayloadCase.DEBUGGERSTART

    override fun handleResponse(message: GenericMessage): Int =
        message.debuggerStarted.sessionId

}