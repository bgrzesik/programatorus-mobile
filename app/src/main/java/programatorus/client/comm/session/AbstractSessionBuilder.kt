package programatorus.client.comm.session

import android.os.Handler
import android.os.Looper
import androidx.core.os.HandlerCompat
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
    ): ISession {
        val theHandler = mHandler ?: handler?: HandlerCompat.createAsync(Looper.getMainLooper())
        return construct(
            client,
            theHandler,
            mClientHandler ?: clientHandler ?: theHandler
        )
    }
}