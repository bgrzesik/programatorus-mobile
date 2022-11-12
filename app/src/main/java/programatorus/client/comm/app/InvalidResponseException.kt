package programatorus.client.comm.app

import programus.proto.Protocol.GenericMessage.PayloadCase

data class InvalidResponseException(
    val expectedPayloadCase: PayloadCase,
    val actualPayloadCase: PayloadCase
) : Exception("invalid response payload expected: $expectedPayloadCase actual: $actualPayloadCase")