package programatorus.client.utils

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class ExecutorTaskRunner(
    private val mExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
) : TaskRunner {

    private var mGoodThread = ThreadLocal<Boolean>()

    init {
        mGoodThread.set(false)
        mExecutor.submit {
            mGoodThread.set(true)
        }.get(Long.MAX_VALUE, TimeUnit.DAYS)
    }

    override val isCurrentThread: Boolean
        get() = mGoodThread.get() == true

    override fun post(task: Runnable) {
        mExecutor.submit(task)
    }

    override fun postDelayed(task: Runnable, timeoutMs: Long) {
        mExecutor.schedule(task, timeoutMs, TimeUnit.MILLISECONDS)
    }
}