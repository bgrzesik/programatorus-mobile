package programatorus.client.comm.transport

import org.hamcrest.CoreMatchers.not
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class LocalTransportTest(
    testName: String,
    provider: TransportProvider
) : TransportTest(testName, provider) {
    @get:Rule
    val androidMocker = AndroidMocker()

    @Before
    override fun assumeRunOnAndroid() {
        Assume.assumeThat(
            "Those tests should be ran on Android",
            System.getProperty("java.specification.vendor"),
            not("The Android Project")
        )
    }
}