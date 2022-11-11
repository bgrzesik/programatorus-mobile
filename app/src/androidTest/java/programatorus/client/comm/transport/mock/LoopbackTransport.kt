package programatorus.client.comm.transport.mock

import programatorus.client.comm.transport.AbstractTransportBuilder
import programatorus.client.comm.transport.ITransport
import programatorus.client.comm.transport.ITransportClient
import programatorus.client.utils.TaskRunner

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
            taskRunner: TaskRunner,
            clientTaskRunner: TaskRunner
        ): ITransport = LoopbackTransport(client, mDisconnectOnReconnect)
    }

}