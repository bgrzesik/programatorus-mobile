package programatorus.client.comm.transport

import android.os.Handler
import android.os.Looper
import programatorus.client.AbstractConnectionBuilder

abstract class AbstractTransportBuilder<T: AbstractTransportBuilder<T>> : AbstractConnectionBuilder<T>(), ITransportBuilder<T> {

    abstract fun construct(
        client: ITransportClient,
        handler: Handler,
        clientHandler: Handler
    ): ITransport

    override fun build(
        client: ITransportClient,
        handler: Handler?,
        clientHandler: Handler?
    ): ITransport =
        construct(
            client,
            mHandler ?: handler ?: Handler(Looper.getMainLooper()),
            mClientHandler ?: clientHandler ?: Handler(Looper.getMainLooper())
        )
}