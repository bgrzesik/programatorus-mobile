package programatorus.client.comm.transport

import android.os.Handler

interface ITransportProvider {

    fun build(
        client: ITransportClient,
        handler: Handler? = null,
        clientHandler: Handler? = null
    ): ITransport

}
