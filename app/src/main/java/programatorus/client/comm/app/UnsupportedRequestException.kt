package programatorus.client.comm.app

import programus.proto.Protocol.GenericMessage.PayloadCase

data class UnsupportedRequestException(
    val payloadCase: PayloadCase
) : Exception("unsupported request $payloadCase")