package programatorus.client.comm.session

import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.AndroidMocker
import programatorus.client.comm.TestUtils

@RunWith(Parameterized::class)
class LocalSessionPairTest(
    testName: String,
    wrapTransport: Boolean,
    wrapMessenger: Boolean,
) : SessionPairTest(testName, wrapTransport, wrapMessenger) {

    @get:Rule
    val androidMocker = AndroidMocker()

    @Before
    override fun assumeRunOnAndroid() = TestUtils.assumeNotAndroid()

}
