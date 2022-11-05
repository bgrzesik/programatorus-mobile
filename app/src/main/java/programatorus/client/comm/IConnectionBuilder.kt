package programatorus.client.comm

import android.os.Handler

interface IConnectionBuilder<B: IConnectionBuilder<B>> {
    fun setHandler(handler: Handler): B

    fun setClientHandler(handler: Handler): B
}