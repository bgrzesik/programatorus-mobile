package programatorus.client.comm

import android.os.Looper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.hamcrest.CoreMatchers
import org.junit.Assume
import programatorus.client.comm.app.IResponder
import programatorus.client.comm.app.RequestRouter
import programatorus.client.comm.presentation.*
import programatorus.client.comm.presentation.mock.IMockMessengerEndpoint
import programatorus.client.comm.presentation.mock.MockMessenger
import programatorus.client.comm.session.ISession
import programatorus.client.comm.session.ISessionClient
import programatorus.client.comm.session.ISessionProvider
import programatorus.client.comm.session.Session
import programatorus.client.comm.transport.ITransportClient
import programatorus.client.comm.transport.ITransportProvider
import programatorus.client.comm.transport.io.IOStreamTransport
import programatorus.client.comm.transport.wrapper.Transport
import programus.proto.Protocol
import programus.proto.Protocol.GenericMessage
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.CompletableFuture


object TestUtils {

    val isAndroid: Boolean
        get() = System.getProperty("java.specification.vendor") == "The Android Project"

    fun assumeAndroid() {
        Assume.assumeThat(
            "Those tests should not be ran on Android",
            System.getProperty("java.specification.vendor"),
            CoreMatchers.`is`("The Android Project")
        )
    }

    fun assumeNotAndroid() {
        Assume.assumeThat(
            "Those tests should not be ran on Android",
            System.getProperty("java.specification.vendor"),
            CoreMatchers.not("The Android Project")
        )
    }

    fun newTestMessage() = GenericMessage.newBuilder()
        .setSessionId(10)
        .setTest(Protocol.TestMessage.newBuilder().apply {
            value = "Test 1234"
        })
        .build()

    fun createMockedSession(
        client: ISessionClient,
        endpoint: IMockMessengerEndpoint
    ): Pair<ISession, MockMessengerOrchestrator> {
        val orchestrator = MockMessengerOrchestrator()
        val session = Session.Builder()
            .setMessenger(
                MockMessenger.Builder()
                    .setOrchestrator(orchestrator)
                    .setEndpoint(endpoint)
            )
            .build(client)

        return Pair(session, orchestrator)
    }

    private fun <T> mockLooper(f: () -> T): T {
        if (isAndroid)
            return f()

        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk("")
        val t = f()
        unmockkAll()

        return t
    }

    fun getMessengerName(v: IMessengerProvider): String =
        mockLooper { v.build(object : IMessageClient {}).toString() }

    fun getTransportName(v: ITransportProvider): String =
        mockLooper { v.build(object : ITransportClient {}).toString() }

    fun getSessionName(v: ISessionProvider): String =
        mockLooper { v.build(object : ISessionClient {
            override fun onRequest(request: GenericMessage) = CompletableFuture.completedFuture(GenericMessage.newBuilder().build())
        }).toString() }

    fun createIOTransportPair(): Pair<IOStreamTransport.Builder, IOStreamTransport.Builder> {
        val leftToRightInput = PipedInputStream()
        val leftToRightOutput = PipedOutputStream(leftToRightInput)

        val rightToLeftInput = PipedInputStream()
        val rightToLeftOutput = PipedOutputStream(rightToLeftInput)

        val left = IOStreamTransport.Builder()
            .setInputStream(leftToRightInput)
            .setOutputStream(rightToLeftOutput)

        val right = IOStreamTransport.Builder()
            .setInputStream(rightToLeftInput)
            .setOutputStream(leftToRightOutput)

        return Pair(left, right)
    }

    fun createTransportPair(): Pair<Transport.Builder, Transport.Builder> {
        val (leftTransport, rightTransport) = createIOTransportPair()

        val left = Transport.Builder()
            .setTransport(leftTransport)

        val right = Transport.Builder()
            .setTransport(rightTransport)

        return Pair(left, right)
    }

    fun createProtocolMessengerPair(wrapTransport: Boolean = false): Pair<ProtocolMessenger.Builder, ProtocolMessenger.Builder> {
        val (leftTransport, rightTransport) = if (wrapTransport) {
            createTransportPair()
        } else {
            createIOTransportPair()
        }

        val left = ProtocolMessenger.Builder()
            .setTransport(leftTransport)

        val right = ProtocolMessenger.Builder()
            .setTransport(rightTransport)

        return Pair(left, right)
    }

    fun createMessengerPair(wrapTransport: Boolean = false): Pair<Messenger.Builder, Messenger.Builder> {
        val (leftMessenger, rightMessenger) = createProtocolMessengerPair(wrapTransport)

        val left = Messenger.Builder()
            .setMessenger(leftMessenger)

        val right = Messenger.Builder()
            .setMessenger(rightMessenger)

        return Pair(left, right)
    }

    fun createSessionPair(wrapTransport: Boolean = false, wrapMessenger: Boolean = false): Pair<Session.Builder, Session.Builder> {
        val (leftMessenger: IMessengerProvider, rightMessenger: IMessengerProvider) = if (wrapMessenger) {
            createMessengerPair(wrapTransport)
        } else {
            createProtocolMessengerPair(wrapTransport)
        }

        val left = Session.Builder()
            .setMessenger(leftMessenger)

        val right = Session.Builder()
            .setMessenger(rightMessenger)

        return Pair(left, right)
    }

    fun createSessionResponderPair(
        leftResponders: List<IResponder>,
        rightResponders: List<IResponder>,
        wrapTransport: Boolean = false,
        wrapMessenger: Boolean = false
    ): Pair<ISession, ISession> {
        val (leftBuilder, rightBuilder) = createSessionPair(wrapTransport, wrapMessenger)
        val left = leftBuilder.build(RequestRouter(leftResponders))
        val right = rightBuilder.build(RequestRouter(rightResponders))
        return Pair(left, right)
    }

}