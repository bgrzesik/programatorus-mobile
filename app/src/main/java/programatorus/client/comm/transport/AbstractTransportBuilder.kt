package programatorus.client.comm.transport

import android.os.Handler
import android.os.Looper
import androidx.core.os.HandlerCompat
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
    ): ITransport {
        val theHandler = mHandler ?: handler?: HandlerCompat.createAsync(Looper.getMainLooper())
        return construct(
            client,
            theHandler,
            mClientHandler ?: clientHandler ?: theHandler
        )
    }
}