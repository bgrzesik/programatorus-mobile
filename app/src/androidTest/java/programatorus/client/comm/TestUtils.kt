package programatorus.client.comm

import android.os.Looper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import programatorus.client.comm.presentation.IMessageClient
import programatorus.client.comm.presentation.IMessenger
import programatorus.client.comm.presentation.IMessengerProvider
import programatorus.client.comm.transport.ITransport
import programatorus.client.comm.transport.ITransportClient
import programatorus.client.comm.transport.ITransportProvider
import programus.proto.Protocol


object TestUtils {

    val isAndroid: Boolean
        get() = System.getProperty("java.specification.vendor") == "The Android Project"

    fun newTestMessage() = Protocol.GenericMessage.newBuilder()
        .setSessionId(10)
        .setTest(Protocol.TestMessage.newBuilder().apply {
            value = "Test 1234"
        })
        .build()

    private fun <T> mockLooper(f: () -> T): T {
        if (isAndroid)
            return f()

        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk("")
        val t = f()
        unmockkAll()

        return t
    }

    fun getMessengerName(v: IMessengerProvider): String =
        mockLooper { v.build(object : IMessageClient {}).toString() }

    fun getTransportName(v: ITransportProvider): String =
        mockLooper { v.build(object : ITransportClient {}).toString() }

}