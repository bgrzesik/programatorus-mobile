package programatorus.client.comm.transport.mock

import android.os.Handler
import android.os.Looper
import android.util.Log
import programatorus.client.comm.transport.*
import java.util.concurrent.CompletableFuture

open class MockTransport(
    private val mMockTransportEndpoint: MockTransportEndpoint,
    private val mClient: ITransportClient,
    private val mHandler: Handler = Handler(Looper.getMainLooper())
) : AbstractTransport(mClient) {

    private val mMessageQueue = ArrayDeque<PendingPacket>()

    companion object {
        const val TAG = "MockTransport"
    }

    override fun send(packet: ByteArray): IOutgoingPacket {
        val pending = PendingPacket(packet)
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

    fun mockPacket(packet: ByteArray) {
        Log.d(TAG, "mockPacket()")
        mHandler.post {
            Log.d(TAG, "mock packet task")

            assert(state == ConnectionState.CONNECTED)
            mClient.onPacketReceived(packet)
        }
    }

    fun mockError() {
        Log.d(TAG, "mockError()")

        mHandler.post {
            Log.d(TAG, "mock error task")

            mClient.onError()
        }
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
                mClient.onPacketReceived(endpointResponse)
            }
        }
    }

}
