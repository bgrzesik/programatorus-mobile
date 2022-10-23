package programatorus.client.comm.presentation.mock

import android.os.Handler
import android.os.Looper
import programatorus.client.comm.presentation.IMessageClient
import programus.proto.Protocol

class LoopbackMessenger(
    client: IMessageClient,
    handler: Handler = Handler(Looper.getMainLooper())
) : MockMessenger(Endpoint(), client, handler) {

    private class Endpoint : IMockMessengerEndpoint {
        override fun onMessage(packet: Protocol.GenericMessage): Protocol.GenericMessage = packet
    }
}