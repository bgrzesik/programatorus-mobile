package programatorus.client.comm.app.proto

import com.google.protobuf.Empty
import programatorus.client.comm.app.PojoResponder
import programus.proto.Protocol.GenericMessage
import programus.proto.Protocol.GenericMessage.PayloadCase

interface OnDebuggerLine : PojoResponder<String, Unit> {

    override val requestPayloadCase: PayloadCase
        get() = PayloadCase.DEBUGGERLINE

    override fun unpackRequest(request: GenericMessage): String =
        request.debuggerLine.line

    override fun prepareResponse(response: Unit): GenericMessage.Builder =
        GenericMessage.newBuilder()
            .setOk(Empty.newBuilder())

}