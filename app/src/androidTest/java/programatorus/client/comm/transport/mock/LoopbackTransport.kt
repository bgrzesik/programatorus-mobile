package programatorus.client.comm.transport.mock

import android.os.Handler
import programatorus.client.comm.transport.AbstractTransportBuilder
import programatorus.client.comm.transport.ITransport
import programatorus.client.comm.transport.ITransportClient

class LoopbackTransport private constructor(
    client: ITransportClient,
    disconnectOnReconnect: Boolean
) : MockTransport(Endpoint(), client, disconnectOnReconnect) {

    override fun toString(): String = "LoopbackTransport"

    private class Endpoint : IMockTransportEndpoint {
        override fun onPacket(packet: ByteArray): ByteArray = packet
    }

    class Builder : AbstractTransportBuilder<Builder>() {
        private var mDisconnectOnReconnect: Boolean = false

        fun setDisconnectOnReconnect(disconnectOnReconnect: Boolean): Builder {
            mDisconnectOnReconnect = disconnectOnReconnect
            return this
        }

        override fun construct(
            client: ITransportClient,
            handler: Handler,
            clientHandler: Handler
        ): ITransport = LoopbackTransport(client, mDisconnectOnReconnect)
    }

}