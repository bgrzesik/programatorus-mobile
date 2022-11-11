package programatorus.client.utils

import android.os.Handler
import android.os.Looper
import androidx.core.os.HandlerCompat


class HandlerTaskRunner(
    private val mHandler: Handler
) : TaskRunner {

    constructor(looper: Looper) : this(HandlerCompat.createAsync(looper))

    override val isCurrentThread: Boolean get() = mHandler.looper.isCurrentThread

    override fun post(task: Runnable) {
        assert(mHandler.post(task))
    }

    override fun postDelayed(task: Runnable, timeoutMs: Long) {
        assert(mHandler.postDelayed(task, timeoutMs))
    }

}