package programatorus.client.comm.transport

import org.hamcrest.CoreMatchers.not
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.AndroidMocker
import programatorus.client.comm.TestUtils

@RunWith(Parameterized::class)
class LocalPacketLoopbackTest(
    testName: String,
    provider: ITransportProvider,
    count: Int
): PacketLoopbackTest(testName, provider, count) {
    @get:Rule
    val androidMocker = AndroidMocker()

    @Before
    override fun assumeRunOnAndroid() = TestUtils.assumeNotAndroid()
}