package programatorus.client.comm.session

import android.os.Handler

interface ISessionProvider {
    fun build(
        client: ISessionClient,
        handler: Handler? = null,
        clientHandler: Handler? = null
    ): ISession
}