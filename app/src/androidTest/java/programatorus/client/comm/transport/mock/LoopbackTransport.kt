package programatorus.client.comm.transport.mock

import android.os.Handler
import programatorus.client.comm.presentation.AbstractMessengerBuilder
import programatorus.client.comm.presentation.IMessageClient
import programatorus.client.comm.presentation.IMessenger
import programatorus.client.comm.presentation.mock.LoopbackMessenger
import programatorus.client.comm.transport.AbstractTransportBuilder
import programatorus.client.comm.transport.ITransport
import programatorus.client.comm.transport.ITransportClient

class LoopbackTransport private constructor(
    client: ITransportClient
) : MockTransport(Endpoint(), client) {

    override fun toString(): String = "LoopbackTransport"

    private class Endpoint : IMockTransportEndpoint {
        override fun onPacket(packet: ByteArray): ByteArray = packet
    }

    class Builder : AbstractTransportBuilder<Builder>() {
        override fun construct(
            client: ITransportClient,
            handler: Handler,
            clientHandler: Handler
        ): ITransport = LoopbackTransport(client)
    }

}