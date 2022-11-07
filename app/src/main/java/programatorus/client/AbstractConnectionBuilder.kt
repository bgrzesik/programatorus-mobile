package programatorus.client

import android.os.Handler
import programatorus.client.comm.IConnectionBuilder

abstract class AbstractConnectionBuilder<B : AbstractConnectionBuilder<B>> : IConnectionBuilder<B> {

    var mHandler: Handler? = null
    var mClientHandler: Handler? = null

    override fun setHandler(handler: Handler): B {
        mHandler = handler
        return this as B
    }

    override fun setClientHandler(handler: Handler): B {
        mClientHandler = handler
        return this as B
    }

}