package programatorus.client.comm

import org.hamcrest.CoreMatchers.not
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.AndroidMocker

@RunWith(Parameterized::class)
class LocalConnectionTest(
    testName: String,
    provider: (IConnectionClient) -> IConnection
) : ConnectionTest(testName, provider) {
    @get:Rule
    val androidMocker = AndroidMocker()

    @Before
    override fun assumeRunOnAndroid() = TestUtils.assumeNotAndroid()
}