package programatorus.client.comm.transport.mock

import android.os.Handler
import android.os.Looper
import programatorus.client.comm.transport.ITransportClient

class LoopbackTransport(
    client: ITransportClient,
    handler: Handler = Handler(Looper.getMainLooper())
) : MockTransport(Endpoint(), client, handler) {

    private class Endpoint : MockTransportEndpoint {
        override fun onPacket(packet: ByteArray): ByteArray = packet
    }

}