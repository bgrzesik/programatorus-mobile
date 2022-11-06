package programatorus.client.comm.transport.mock

import android.os.Handler
import android.util.Log
import programatorus.client.comm.AbstractConnection
import programatorus.client.comm.transport.*
import java.util.concurrent.CompletableFuture

open class MockTransport internal constructor(
    private val mMockTransportEndpoint: IMockTransportEndpoint,
    private val mClient: ITransportClient,
    private val mDisconnectOnReconnect: Boolean,
) : AbstractConnection(mClient), ITransport {

    private val mMessageQueue = ArrayDeque<PendingPacket>()

    companion object {
        const val TAG = "MockTransport"
    }

    override fun send(packet: ByteArray): IOutgoingPacket {
        val pending = PendingPacket(packet)
        mMessageQueue.addLast(pending)
        pumpPendingMessages()
        return pending
    }

    private fun pumpPendingMessages() {
        Log.d(TAG, "pumpPendingMessages()")

        if (state != ConnectionState.CONNECTED) {
            reconnect()
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
            state = ConnectionState.DISCONNECTING
            state = ConnectionState.DISCONNECTED
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

    fun mockPacket(packet: ByteArray) {
        Log.d(TAG, "mockPacket()")
        assert(state == ConnectionState.CONNECTED)
        mClient.onPacketReceived(packet)
    }

    fun mockError() {
        Log.d(TAG, "mockError()")
        mClient.onError()
    }

    private inner class PendingPacket(
        override val packet: ByteArray
    ) : IOutgoingPacket {
        override val response: CompletableFuture<IOutgoingPacket> = CompletableFuture()

        fun send() {
            assert(state == ConnectionState.CONNECTED)

            Log.d(TAG, "send(): Sending to mocked endpoint")
            val endpointResponse = mMockTransportEndpoint.onPacket(packet)

            response.complete(this)

            if (endpointResponse != null) {
                Log.d(TAG, "send(): Mocked endpoint response")
                mockPacket(endpointResponse)
            }
        }
    }

    override fun toString(): String = "MockTransport[$mMockTransportEndpoint]"

    class Builder : AbstractTransportBuilder<Builder>() {
        private var mEndpoint: IMockTransportEndpoint? = null
        private var mDisconnectOnReconnect: Boolean = false

        fun setEndpoint(endpoint: IMockTransportEndpoint): Builder {
            mEndpoint = endpoint
            return this
        }

        fun setDisconnectOnReconnect(disconnectOnReconnect: Boolean): Builder {
            mDisconnectOnReconnect = disconnectOnReconnect
            return this
        }

        override fun construct(
            client: ITransportClient,
            handler: Handler,
            clientHandler: Handler
        ): ITransport = MockTransport(mEndpoint!!, client, mDisconnectOnReconnect)
    }
}
