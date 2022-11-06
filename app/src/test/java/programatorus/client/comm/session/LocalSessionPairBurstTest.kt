package programatorus.client.comm.session

import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.AndroidMocker
import programatorus.client.comm.TestUtils

@RunWith(Parameterized::class)
class LocalSessionPairBurstTest(
    testName: String,
    wrapTransport: Boolean,
    wrapMessenger: Boolean,
    count: Int
) : SessionPairBurstTest(testName, wrapTransport, wrapMessenger, count) {

    @get:Rule
    val androidMocker = AndroidMocker()

    @Before
    override fun assumeRunOnAndroid() = TestUtils.assumeNotAndroid()

}
