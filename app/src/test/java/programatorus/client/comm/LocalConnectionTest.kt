package programatorus.client.comm

import org.hamcrest.CoreMatchers.not
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import programatorus.client.comm.transport.AndroidMocker
import programatorus.client.comm.transport.ITransport
import programatorus.client.comm.transport.ITransportClient

@RunWith(Parameterized::class)
class LocalConnectionTest(
    testName: String,
    provider: (IConnectionClient) -> IConnection
) : ConnectionTest(testName, provider) {
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