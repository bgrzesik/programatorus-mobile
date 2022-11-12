package programatorus.client.comm.presentation.mock

import android.util.Log
import programatorus.client.comm.AbstractConnection
import programatorus.client.comm.presentation.*
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.utils.TaskRunner
import programus.proto.Protocol
import java.util.concurrent.CompletableFuture

open class MockMessenger internal constructor(
    private val mMockMessengerEndpoint: IMockMessengerEndpoint,
    private val mClient: IMessageClient,
    private val mDisconnectOnReconnect: Boolean,
) : AbstractConnection(mClient), IMessenger {

    private val mMessageQueue = ArrayDeque<PendingMessage>()

    companion object {
        const val TAG = "MockMessenger"
    }

    override fun send(message: Protocol.GenericMessage): IOutgoingMessage {
        val pending = PendingMessage(message)
        mMessageQueue.addLast(pending)
        pumpPendingMessages()
        return pending
    }

    private fun pumpPendingMessages() {
        Log.d(TAG, "pumpPendingMessages()")

        if (state != ConnectionState.CONNECTED) {
            reconnect()
            this.pumpPendingMessages()
            return
        }

        while (!mMessageQueue.isEmpty()) {
            val packet = mMessageQueue.removeFirst()
            packet.send()
        }
    }


    override fun reconnect() {
        Log.d(TAG, "reconnect()")
        if (state == ConnectionState.CONNECTED) {
            Log.d(TAG, "already connected")
            if (!mDisconnectOnReconnect) {
                return
            }
            disconnect()
        }

        assert(state == ConnectionState.DISCONNECTED)

        state = ConnectionState.CONNECTING
        state = ConnectionState.CONNECTED
    }

    override fun disconnect() {
        Log.d(TAG, "disconnect()")

        if (state == ConnectionState.DISCONNECTED) {
            Log.d(TAG, "already disconnected")
            return
        }

        assert(state == ConnectionState.CONNECTED)

        state = ConnectionState.DISCONNECTING
        state = ConnectionState.DISCONNECTED
    }

    fun mockMessage(packet: Protocol.GenericMessage) {
        Log.d(TAG, "mockPacket()")

        assert(state == ConnectionState.CONNECTED)
        mClient.onMessageReceived(packet)
    }

    fun mockError() {
        Log.d(TAG, "mockError()")

        mClient.onError()
    }

    override fun toString(): String = "MockMessenger[$mMockMessengerEndpoint]"

    private inner class PendingMessage(
        override val message: Protocol.GenericMessage
    ) : IOutgoingMessage {
        override val response: CompletableFuture<IOutgoingMessage> = CompletableFuture()

        fun send() {
            assert(state == ConnectionState.CONNECTED)

            Log.d(TAG, "Sending to mocked endpoint")
            val endpointResponse = mMockMessengerEndpoint.onMessage(message)

            response.complete(this)

            if (endpointResponse != null) {
                Log.d(TAG, "Mocked endpoint response")
                mockMessage(endpointResponse)
            }
        }
    }


    class Builder : AbstractMessengerBuilder<Builder>() {
        private var mEndpoint: IMockMessengerEndpoint? = null
        private var mDisconnectOnReconnect: Boolean = false
        private var mOrchestrator: MockMessengerOrchestrator? = null

        fun setEndpoint(endpoint: IMockMessengerEndpoint): Builder {
            mEndpoint = endpoint
            return this
        }

        fun setDisconnectOnReconnect(disconnectOnReconnect: Boolean): Builder {
            mDisconnectOnReconnect = disconnectOnReconnect
            return this
        }

        fun setOrchestrator(orchestrator: MockMessengerOrchestrator): Builder {
            mOrchestrator = orchestrator
            return this
        }

        override fun construct(
            client: IMessageClient,
            taskRunner: TaskRunner,
            clientTaskRunner: TaskRunner
        ): IMessenger {
            val mockMessenger = MockMessenger(mEndpoint!!, client, mDisconnectOnReconnect)
            mOrchestrator?.register(mockMessenger)
            return mockMessenger
        }
    }

}
