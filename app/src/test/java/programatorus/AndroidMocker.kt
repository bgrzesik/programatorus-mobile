package programatorus

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import programatorus.client.comm.TestUtils
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

class AndroidMocker : TestWatcher() {
    fun mock() {
        mockkStatic(Log::class)

        every { Log.d(any(), any()) } answers { println("D/${args[0]}: ${args[1]}"); 1 }
        every { Log.e(any(), any()) } answers { println("E/${args[0]}: ${args[1]}"); 1 }
        every { Log.i(any(), any()) } answers { println("I/${args[0]}: ${args[1]}"); 1 }
    }


    override fun starting(description: Description) {
        super.starting(description)
        if (!TestUtils.isAndroid) {
            mock()
        }
    }

}