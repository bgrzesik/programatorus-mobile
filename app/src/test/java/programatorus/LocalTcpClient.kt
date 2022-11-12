package programatorus

import android.util.Log
import com.google.protobuf.Empty
import programatorus.client.comm.presentation.Messenger
import programatorus.client.comm.presentation.ProtocolMessenger
import programatorus.client.comm.session.ISessionClient
import programatorus.client.comm.session.Session
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.comm.transport.net.TcpTransport
import programatorus.client.comm.transport.wrapper.Transport
import programus.proto.Protocol.GenericMessage
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Semaphore


const val TAG = "LocalTcpClient"

fun main() {
    val mocker = AndroidMocker()
    mocker.mock()

    val semaphore = Semaphore(1)
    semaphore.acquire()

    val session = Session.Builder()
        .setMessenger(
            Messenger.Builder()
                .setMessenger(
                    ProtocolMessenger.Builder()
                        .setTransport(
                            Transport.Builder()
                                .setTransport(
                                    TcpTransport.Builder()
                                        .setHost("127.0.0.1")
                                        .setPort(2137)
                                )
                        )
                )
        )
        .build(object : ISessionClient {
            override fun onRequest(request: GenericMessage): CompletableFuture<GenericMessage> {
                Log.i(TAG, "Got request $request")

                return CompletableFuture.completedFuture(
                    GenericMessage.newBuilder()
                        .setOk(Empty.getDefaultInstance())
                        .build()
                )
            }

            override fun onStateChanged(state: ConnectionState) {
                Log.i(TAG, "State changed -> $state")
            }
        })
    session.reconnect()

    semaphore.acquire()
}
