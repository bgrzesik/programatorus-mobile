package programatorus.client.comm.session

import android.os.Handler
import android.os.Looper
import programatorus.client.AbstractConnectionBuilder

abstract class AbstractSessionBuilder<S: AbstractSessionBuilder<S>> : AbstractConnectionBuilder<S>(), ISessionBuilder<S> {

    abstract fun construct(
        client: ISessionClient,
        handler: Handler,
        clientHandler: Handler
    ): ISession

    override fun build(
        client: ISessionClient,
        handler: Handler?,
        clientHandler: Handler?
    ): ISession =
        construct(
            client,
            mHandler ?: handler ?: Handler(Looper.getMainLooper()),
            mClientHandler ?: clientHandler ?: Handler(Looper.getMainLooper())
        )
}