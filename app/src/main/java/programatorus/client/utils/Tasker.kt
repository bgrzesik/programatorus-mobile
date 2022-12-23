package programatorus.client.utils

import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

interface Tasker {
    companion object {
        const val TAG = "HandlerActor"
    }

    val taskRunner: TaskRunner

    fun assertRunOnLooper(target: TaskRunner = taskRunner) {
        if (!taskRunner.isCurrentThread) {
            Log.e(TAG, "", RuntimeException("Method ran on invalid thread"))
        }
        assert(taskRunner.isCurrentThread) { "Method ran on invalid thread" }
    }

    fun <T> assertLooper(target: TaskRunner = taskRunner, func: () -> T): T {
        assertRunOnLooper()
        return func()
    }

    fun runGuardedOnLooper(
        guard: AtomicBoolean,
        timeout: Long? = null,
        enforcePost: Boolean = false,
        target: TaskRunner = taskRunner,
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
        target: TaskRunner = taskRunner,
        func: () -> Unit
    ) {
        if (taskRunner.isCurrentThread && timeout == null && !enforcePost) {
            assertRunOnLooper()
            func()
        } else {
            if (timeout != null) {
                taskRunner.postDelayed(func, timeout)
            } else {
                taskRunner.post(func)
            }
        }
    }

}