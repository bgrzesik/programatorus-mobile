package programatorus.client.comm.transport

import org.hamcrest.CoreMatchers.not
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class LocalPacketLoopbackTest(
    testName: String,
    provider: (ITransportClient) -> ITransport,
    count: Int
): PacketLoopbackTest(testName, provider, count) {
    @get:Rule
    val androidMocker = AndroidMocker()

    @Before
    override fun assumeRunOnAndroid() {
        Assume.assumeThat(
            "Those tests should not be ran on Android",
            System.getProperty("java.specification.vendor"),
            not("The Android Project")
        )
    }
}