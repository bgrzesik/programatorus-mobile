package programatorus

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import programatorus.client.comm.TestUtils

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