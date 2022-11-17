package programatorus.client.comm.session

import org.junit.Test
import programatorus.client.comm.presentation.mock.IMockMessengerEndpoint
import programatorus.client.comm.presentation.mock.MockMessenger
import programatorus.client.comm.transport.ConnectionState
import programus.proto.Protocol
import java.util.concurrent.CountDownLatch

open class SessionTest {

    @Test(timeout = 60000)
    fun testKeepAliveDisconnect() {
        val latch = CountDownLatch(1)

        val endpoint = object : IMockMessengerEndpoint {
            override fun onMessage(packet: Protocol.GenericMessage): Protocol.GenericMessage? {
                return null
            }
        }

        val client = object: ISessionClient, SessionClientFailOnRequest {
            override fun onStateChanged(state: ConnectionState) {
                if (state == ConnectionState.DISCONNECTED) {
                    latch.countDown()
                }
            }
        }

        val session = Session.Builder()
            .setMessenger(MockMessenger.Builder()
                .setEndpoint(endpoint)
                .setDisconnectOnReconnect(true))
            .build(client)

        session.reconnect()
        latch.await()
        session.disconnect()
    }

}