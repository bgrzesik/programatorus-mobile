package programatorus.client.comm.presentation

import android.os.Handler
import android.os.Looper
import programatorus.client.AbstractConnectionBuilder

abstract class AbstractMessengerBuilder<M : AbstractMessengerBuilder<M>> : AbstractConnectionBuilder<M>(), IMessengerBuilder<M> {

    abstract fun construct(
        client: IMessageClient,
        handler: Handler,
        clientHandler: Handler
    ): IMessenger

    override fun build(
        client: IMessageClient,
        handler: Handler?,
        clientHandler: Handler?
    ): IMessenger =
        construct(
            client,
            mHandler ?: handler ?: Handler(Looper.getMainLooper()),
            mClientHandler ?: clientHandler ?: Handler(Looper.getMainLooper())
        )
}