package programatorus.client.comm.presentation

import org.hamcrest.CoreMatchers
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.AndroidMocker
import programatorus.client.comm.TestUtils

@RunWith(Parameterized::class)
class LocalMessageLoopbackTest(
    testName: String,
    provider: IMessengerProvider,
    count: Int
) : MessageLoopbackTest(testName, provider, count) {
    @get:Rule
    val androidMocker = AndroidMocker()

    @Before
    override fun assumeRunOnAndroid() = TestUtils.assumeNotAndroid()

}
