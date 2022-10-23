package programatorus.client.comm.transport.mock

import android.util.Log
import programatorus.client.comm.AbstractConnection
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.comm.transport.IOutgoingPacket
import programatorus.client.comm.transport.ITransport
import programatorus.client.comm.transport.ITransportClient
import java.util.concurrent.CompletableFuture

open class MockTransport(
    private val mMockTransportEndpoint: IMockTransportEndpoint,
    private val mClient: ITransportClient,
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

            Log.d(TAG, "Sending to mocked endpoint")
            val endpointResponse = mMockTransportEndpoint.onPacket(packet)

            response.complete(this)

            if (endpointResponse != null) {
                Log.d(TAG, "Mocked endpoint response")
                mockPacket(endpointResponse)
            }
        }
    }

    override fun toString(): String = "MockTransport[$mMockTransportEndpoint]"
}
