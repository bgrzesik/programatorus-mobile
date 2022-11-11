package programatorus.client.comm.presentation.mock

import programatorus.client.comm.presentation.AbstractMessengerBuilder
import programatorus.client.comm.presentation.IMessageClient
import programatorus.client.comm.presentation.IMessenger
import programatorus.client.utils.TaskRunner
import programus.proto.Protocol

class LoopbackMessenger private constructor(
    client: IMessageClient,
    disconnectOnReconnect: Boolean,
) : MockMessenger(Endpoint(), client, disconnectOnReconnect) {

    override fun toString(): String = "LoopbackMessenger"

    private class Endpoint : IMockMessengerEndpoint {
        override fun onMessage(packet: Protocol.GenericMessage): Protocol.GenericMessage = packet
    }

    class Builder : AbstractMessengerBuilder<Builder>() {
        private var mDisconnectOnReconnect: Boolean = false

        fun setDisconnectOnReconnect(disconnectOnReconnect: Boolean): Builder {
            mDisconnectOnReconnect = disconnectOnReconnect
            return this
        }

        override fun construct(
            client: IMessageClient,
            taskRunner: TaskRunner,
            clientTaskRunner: TaskRunner
        ): IMessenger = LoopbackMessenger(client, mDisconnectOnReconnect)
    }

}