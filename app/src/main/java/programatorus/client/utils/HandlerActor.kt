package programatorus.client.utils

import android.os.Handler
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

interface HandlerActor {
    companion object {
        const val TAG = "HandlerActor"
    }

    val handler: Handler

    fun assertRunOnLooper(targetHandler: Handler = handler) {
        if (!handler.looper.isCurrentThread) {
            Log.e(TAG, "", RuntimeException("Method ran on invalid thread"))
        }
        assert(handler.looper.isCurrentThread) { "Method ran on invalid thread" }
    }

    fun <T> assertLooper(targetHandler: Handler = handler, func: () -> T): T {
        assertRunOnLooper()
        return func()
    }

    fun runGuardedOnLooper(
        guard: AtomicBoolean,
        timeout: Long? = null,
        enforcePost: Boolean = false,
        targetHandler: Handler = handler,
        func: () -> Unit
    ) {
        if (!guard.compareAndSet(false, true)) {
            return
        }

        runOnLooper(timeout, enforcePost) {
            assertRunOnLooper()
            guard.set(false)
            func()
        }
    }

    fun runOnLooper(
        timeout: Long? = null,
        enforcePost: Boolean = false,
        targetHandler: Handler = handler,
        func: () -> Unit
    ) {
        if (handler.looper.isCurrentThread && timeout == null && !enforcePost) {
            assertRunOnLooper()
            func()
        } else {
            if (timeout != null) {
                assert(handler.postDelayed(func, timeout))
            } else {
                assert(handler.post(func))
            }
        }
    }

}