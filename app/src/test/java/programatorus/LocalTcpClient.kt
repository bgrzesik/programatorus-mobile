package programatorus

import android.util.Log
import com.google.protobuf.Empty
import programatorus.client.comm.app.RequestRouter
import programatorus.client.comm.app.proto.FileUpload
import programatorus.client.comm.app.proto.GetBoards
import programatorus.client.comm.presentation.Messenger
import programatorus.client.comm.presentation.ProtocolMessenger
import programatorus.client.comm.session.ISessionClient
import programatorus.client.comm.session.Session
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.comm.transport.net.TcpTransport
import programatorus.client.comm.transport.wrapper.Transport
import programus.proto.Protocol.GenericMessage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Semaphore


const val TAG = "LocalTcpClient"

fun main() {
    val semaphore = Semaphore(1)
    semaphore.acquire()

    val router = RequestRouter(listOf())

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
        .build(router)
    session.reconnect()

    val boards = GetBoards().request(session).get()
    Log.i(TAG, "Got boards $boards")

    val bytes = ByteArray(1024) { 0xaa.toByte() }
    val stream = ByteArrayInputStream(bytes)
    FileUpload.upload(session, "test.bin", bytes.size, stream).get()

    session.disconnect()
}
