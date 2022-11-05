package programatorus.client.comm.presentation

import android.os.Handler

interface IMessengerProvider {

    fun build(
        client: IMessageClient,
        handler: Handler? = null,
        clientHandler: Handler? = null
    ): IMessenger

}