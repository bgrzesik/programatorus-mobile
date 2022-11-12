package programatorus.client.utils

import android.os.Looper
import androidx.core.os.HandlerCompat

interface TaskRunner {

    val isCurrentThread: Boolean

    fun post(task: Runnable)

    fun postDelayed(task: Runnable, timeoutMs: Long)

    companion object {
        fun constructDefault(): TaskRunner =
            if (System.getProperty("java.specification.vendor") == "The Android Project") {
                HandlerTaskRunner(HandlerCompat.createAsync(Looper.getMainLooper()))
            } else {
                ExecutorTaskRunner()
            }
    }
}