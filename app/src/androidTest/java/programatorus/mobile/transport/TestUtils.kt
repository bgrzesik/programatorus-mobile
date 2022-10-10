package programatorus.mobile.transport

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import programus.proto.GenericMessage
import programus.proto.TestMessage

object TestUtils {

    fun mockLog() {
        fun log(level: String, tag: String, message: String): Int {
            println("$tag/$level: $message")
            return 0
        }

        mockkStatic(Log::class)
        every { Log.v(any(), any()) } answers { log("V", firstArg(), secondArg()) }
        every { Log.d(any(), any()) } answers { log("D", firstArg(), secondArg()) }
        every { Log.i(any(), any()) } answers { log("I", firstArg(), secondArg()) }
        every { Log.e(any(), any()) } answers { log("E", firstArg(), secondArg()) }
    }

    fun newTestMessage() = GenericMessage.newBuilder()
        .setSessionId(10)
        .setTest(TestMessage.newBuilder().apply {
            value = "Test 1234"
        })
        .build()

}