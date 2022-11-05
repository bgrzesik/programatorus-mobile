package programatorus.client.comm.presentation.mock

import android.os.Handler
import android.os.Looper
import programatorus.client.comm.presentation.AbstractMessengerBuilder
import programatorus.client.comm.presentation.IMessageClient
import programatorus.client.comm.presentation.IMessenger
import programus.proto.Protocol

class LoopbackMessenger private constructor(
    client: IMessageClient,
    handler: Handler = Handler(Looper.getMainLooper())
) : MockMessenger(Endpoint(), client, handler) {

    override fun toString(): String = "LoopbackMessenger"

    private class Endpoint : IMockMessengerEndpoint {
        override fun onMessage(packet: Protocol.GenericMessage): Protocol.GenericMessage = packet
    }

    class Builder : AbstractMessengerBuilder<Builder>() {
        override fun construct(
            client: IMessageClient,
            handler: Handler,
            clientHandler: Handler
        ): IMessenger = LoopbackMessenger(client, handler)
    }

}