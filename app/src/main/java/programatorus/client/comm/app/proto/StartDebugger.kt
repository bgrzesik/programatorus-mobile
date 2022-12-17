package programatorus.client.comm.app.proto

import programatorus.client.comm.app.IRequester
import programatorus.client.model.Board
import programatorus.client.model.Firmware
import programus.proto.Protocol.DebuggerStart
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase

class StartDebugger(
    val board: Board,
    val firmware: Firmware,
) : IRequester<Int> {

    override fun prepareRequest(): GenericMessage.Builder =
        GenericMessage.newBuilder()
            .setDebuggerStart(
                DebuggerStart.newBuilder()
                    .setTarget(board.name)
                    .setFirmware(firmware.name)
            )

    override val responsePayloadCase: PayloadCase
        get() = PayloadCase.DEBUGGERSTART

    override fun handleResponse(message: GenericMessage): Int =
        message.debuggerStarted.sessionId

}