package programatorus.client.comm

import android.os.Looper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import programatorus.client.comm.presentation.IMessageClient
import programatorus.client.comm.presentation.IMessenger
import programatorus.client.comm.transport.ITransport
import programatorus.client.comm.transport.ITransportClient
import programus.proto.Protocol


object TestUtils {
    fun newTestMessage() = Protocol.GenericMessage.newBuilder()
        .setSessionId(10)
        .setTest(Protocol.TestMessage.newBuilder().apply {
            value = "Test 1234"
        })
        .build()

    fun getMessengerName(v: (IMessageClient) -> IMessenger): String {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk("")
        val name = v(object : IMessageClient {}).toString()
        unmockkAll()

        return name;
    }

    fun getTransportName(v: (ITransportClient) -> ITransport): String {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk("")
        val name = v(object : ITransportClient {}).toString()
        unmockkAll()

        return name;
    }

}