package programatorus.client.comm.presentation

import org.hamcrest.CoreMatchers
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.client.comm.transport.AndroidMocker
import programatorus.client.comm.transport.ITransport
import programatorus.client.comm.transport.ITransportClient
import programatorus.client.comm.transport.PacketLoopbackTest

@RunWith(Parameterized::class)
class LocalMessageLoopbackTest(
    testName: String,
    provider: (IMessageClient) -> IMessenger,
    count: Int
) : MessageLoopbackTest(testName, provider, count) {
    @get:Rule
    val androidMocker = AndroidMocker()

    @Before
    override fun assumeRunOnAndroid() {
        Assume.assumeThat(
            "Those tests should not be ran on Android",
            System.getProperty("java.specification.vendor"),
            CoreMatchers.not("The Android Project")
        )
    }
}
