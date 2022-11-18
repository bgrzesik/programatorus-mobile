package programatorus.client.comm.app

import programus.proto.Protocol.GenericMessage.PayloadCase

class InvalidResponseException(message: String) : Exception(message) {

    constructor(
        expectedPayloadCase: PayloadCase,
        actualPayloadCase: PayloadCase
    ): this("invalid response payload expected: $expectedPayloadCase actual: $actualPayloadCase")
}