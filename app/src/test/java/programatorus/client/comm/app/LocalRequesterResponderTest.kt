package programatorus.client.comm.app

import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.AndroidMocker
import programatorus.client.comm.TestUtils

@RunWith(Parameterized::class)
class LocalRequesterResponderTest(
    testName: String,
    wrapTransport: Boolean,
    wrapMessenger: Boolean,
) : RequesterResponderTest(testName, wrapTransport, wrapMessenger) {

    @get:Rule
    val androidMocker = AndroidMocker()

    @Before
    override fun assumeRunOnAndroid() = TestUtils.assumeNotAndroid()

}
