package programatorus.client.comm.presentation

import android.os.Handler
import android.os.Looper
import androidx.core.os.HandlerCompat
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
    ): IMessenger {
        val theHandler = mHandler ?: handler?: HandlerCompat.createAsync(Looper.getMainLooper())
        return construct(
            client,
            theHandler,
            mClientHandler ?: clientHandler ?: theHandler
        )
    }
}