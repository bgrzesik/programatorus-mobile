package programatorus.client.comm.app

import org.junit.Before
import org.junit.Rule
import programatorus.AndroidMocker
import programatorus.client.comm.TestUtils

class LocalResponderTest: ResponderTest() {
    @get:Rule
    val androidMocker = AndroidMocker()

    @Before
    override fun assumeRunOnAndroid() = TestUtils.assumeNotAndroid()
}