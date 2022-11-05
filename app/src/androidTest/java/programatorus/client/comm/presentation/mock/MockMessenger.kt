package programatorus.client.comm.presentation.mock

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import programatorus.client.comm.presentation.AbstractMessengerBuilder
import programatorus.client.comm.presentation.IMessageClient
import programatorus.client.comm.presentation.IMessenger
import programatorus.client.comm.presentation.IOutgoingMessage
import programatorus.client.comm.transport.*
import programatorus.client.comm.transport.bt.BluetoothTransport
import programus.proto.Protocol
import java.util.concurrent.CompletableFuture

open class MockMessenger internal constructor(
    private val mMockMessengerEndpoint: IMockMessengerEndpoint,
    private val mClient: IMessageClient,
    private val mHandler: Handler = Handler(Looper.getMainLooper())
) : IMessenger {

    private val mMessageQueue = ArrayDeque<PendingMessage>()

    companion object {
        const val TAG = "MockMessenger"
    }

    override var state: ConnectionState = ConnectionState.DISCONNECTED
        set(value) {
            if (field != value) {
                mClient.onStateChanged(value)
                field = value
            }
        }

    override fun send(message: Protocol.GenericMessage): IOutgoingMessage {
        val pending = PendingMessage(message)
        mHandler.post {
            mMessageQueue.addLast(pending)
            pumpPendingMessages()
        }
        return pending
    }

    private fun pumpPendingMessages() {
        Log.d(TAG, "pumpPendingMessages()")

        if (state != ConnectionState.CONNECTED) {
            reconnect()
            mHandler.post(this::pumpPendingMessages)
            return;
        }

        while (!mMessageQueue.isEmpty()) {
            val packet = mMessageQueue.removeFirst()
            packet.send()
        }
    }


    override fun reconnect() {
        Log.d(TAG, "reconnect()")
        mHandler.post {
            Log.d(TAG, "reconnect task")
            if (state == ConnectionState.CONNECTED) {
                Log.d(TAG, "already connected")
                return@post
            }

            assert(state == ConnectionState.DISCONNECTED)

            state = ConnectionState.CONNECTING
            state = ConnectionState.CONNECTED
        }
    }

    override fun disconnect() {
        Log.d(TAG, "disconnect()")
        mHandler.post {
            Log.d(TAG, "disconnect task")

            if (state == ConnectionState.DISCONNECTED) {
                Log.d(TAG, "already disconnected")
                return@post
            }

            assert(state == ConnectionState.CONNECTED)

            state = ConnectionState.DISCONNECTING
            state = ConnectionState.DISCONNECTED
        }
    }

    fun mockPacket(packet: Protocol.GenericMessage) {
        Log.d(TAG, "mockPacket()")
        mHandler.post {
            Log.d(TAG, "mock packet task")

            assert(state == ConnectionState.CONNECTED)
            mClient.onMessageReceived(packet)
        }
    }

    fun mockError() {
        Log.d(TAG, "mockError()")

        mHandler.post {
            Log.d(TAG, "mock error task")

            mClient.onError()
        }
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
                mClient.onMessageReceived(endpointResponse)
            }
        }
    }


    class Builder : AbstractMessengerBuilder<Builder>() {
        private var mEndpoint: IMockMessengerEndpoint? = null

        fun setEndpoint(endpoint: IMockMessengerEndpoint): Builder {
            mEndpoint = endpoint
            return this
        }

        override fun construct(
            client: IMessageClient,
            handler: Handler,
            clientHandler: Handler
        ): IMessenger = MockMessenger(mEndpoint!!, client, handler)
    }

}
