package programatorus.client.comm.session

import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.AndroidMocker
import programatorus.client.comm.TestUtils

class LocalSessionTest : SessionTest() {

    @get:Rule
    val androidMocker = AndroidMocker()

    @Before
    override fun assumeRunOnAndroid() = TestUtils.assumeNotAndroid()

}
