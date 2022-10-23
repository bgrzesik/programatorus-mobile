package programatorus.client.comm.transport

import android.os.Handler
import android.os.Looper
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class AndroidMocker : TestWatcher() {
    private var mScheduler: ScheduledExecutorService? = null

    private val isAndroid : Boolean
        get() = "The Android Project" == System.getProperty("java.specification.vendor")

    private fun mock() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk()

        mockkStatic(Log::class)

        every { Log.d(any(), any()) } answers { println("D/${args[0]}: ${args[1]}"); 1 }
        every { Log.e(any(), any()) } answers { println("E/${args[0]}: ${args[1]}"); 1 }
        every { Log.i(any(), any()) } answers { println("I/${args[0]}: ${args[1]}"); 1 }

        mockkConstructor(Handler::class)

        every { anyConstructed<Handler>().post(any()) } answers {
            mScheduler!!.submit(arg(0))
            true
        }
        every { anyConstructed<Handler>().postDelayed(any(), any()) } answers {
            mScheduler!!.schedule(arg(0), arg(1), TimeUnit.MILLISECONDS)
            true
        }
    }

    override fun starting(description: Description) {
        super.starting(description)

        mScheduler = Executors.newScheduledThreadPool(1)

        if (isAndroid) {
            return
        }

        mock()
    }

    override fun finished(description: Description) {
        super.finished(description)

        if (mScheduler != null) {
            mScheduler!!.shutdown()
            mScheduler!!.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)
            mScheduler = null
        }
    }

}