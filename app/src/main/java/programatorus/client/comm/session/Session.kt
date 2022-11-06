package programatorus.client.comm.session

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.protobuf.Descriptors
import com.google.protobuf.Empty
import programatorus.client.comm.presentation.IMessageClient
import programatorus.client.comm.presentation.IMessengerProvider
import programatorus.client.comm.presentation.IOutgoingMessage
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.utils.HandlerActor
import programus.proto.Protocol
import programus.proto.Protocol.GenericMessage
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class Session private constructor(
    messenger: IMessengerProvider,
    client: ISessionClient,
    private val mHandler: Handler = Handler(Looper.getMainLooper()),
    private val mClientHandler: Handler = Handler(Looper.getMainLooper())
) : ISession, HandlerActor {

    private companion object {
        const val TAG = "Session"

        const val HEARTBEAT_MS: Long = 500
        const val TIMEOUT_MS: Long = 8 * HEARTBEAT_MS

        val REQUEST_ID_FIELD: Descriptors.FieldDescriptor =
            GenericMessage.getDescriptor()
                .findFieldByNumber(GenericMessage.REQUESTID_FIELD_NUMBER)

        val RESPONSE_ID_FIELD: Descriptors.FieldDescriptor =
            GenericMessage.getDescriptor()
                .findFieldByNumber(GenericMessage.RESPONSEID_FIELD_NUMBER)
    }


    private var mSessionId: Long? = null
    private var mLastTransferMs = System.currentTimeMillis()
    private var mPostedHeartbeat: CompletableFuture<GenericMessage>? = null
    private val mNextRequestId = AtomicLong(0)
    private val mPumpPending = AtomicBoolean(false)

    private val mWaitingForResponse = mutableMapOf<Long, PendingMessage>()

    private val mClient = Client(client)
    private val mQueue = LinkedBlockingQueue<PendingMessage>()
    private val mMessenger = messenger.build(mClient, mClientHandler, mClientHandler)

    override val handler: Handler
        get() = mHandler

    override fun request(message: GenericMessage): CompletableFuture<GenericMessage> {
        Log.d(TAG, "request():")

        val pending = PendingMessage(
            true,
            GenericMessage.newBuilder(message)
                .setSessionId(
                    mSessionId ?: -1
                ) // TODO(bgrzesik): Initialize the session and assign a session ID
                .setRequestId(mNextRequestId.incrementAndGet())
                .build()
        )

        mQueue.add(pending)
        pumpMessages()
        return pending.future
    }

    override val state: ConnectionState
        get() = mMessenger.state

    override fun reconnect() = mMessenger.reconnect()

    override fun disconnect() = mMessenger.disconnect()

    private fun processControlRequests(message: GenericMessage): GenericMessage? = assertLooper {
        Log.d(TAG, "processControlRequests(): payLoadCase=${message.payloadCase}")
        when (message.payloadCase) {
            GenericMessage.PayloadCase.HEARTBEAT -> {
                Log.i(TAG, "processControlRequests(): Responding to heartbeat")
                GenericMessage.newBuilder()
                    .setOk(Empty.getDefaultInstance())
                    .build()
            }

            GenericMessage.PayloadCase.SETSESSIONID -> {
                Log.i(TAG, "processControlRequests(): Setting sessionId=$mSessionId")
                mSessionId = message.setSessionId.sessionId

                GenericMessage.newBuilder()
                    .setOk(Empty.getDefaultInstance())
                    .build()
            }

            else -> null // Don't respond to this
        }
    }

    private fun onRequestDone(
        requestId: Long,
        response: GenericMessage?,
        exception: Throwable?
    ) = runOnLooper {
        Log.d(TAG, "onRequestDone(): requestId=$requestId")
        var response = response

        if (exception != null) {
            Log.e(TAG, "onRequest(): ")
            response = GenericMessage.newBuilder()
                .setResponseId(requestId)
                .setSessionId(mSessionId ?: -1)
                .setError( // TODO(bgrzesik): proper error mapping
                    Protocol.ErrorMessage.newBuilder()
                        .setDescription(exception.message)
                )
                .build()
        } else {
            response = GenericMessage.newBuilder(response)
                .clearRequestId()
                .setResponseId(requestId)
                .setSessionId(mSessionId ?: -1)
                .build()
        }

        Log.d(TAG, "onRequestDone(): requestId=$requestId responsePayloadCase=${response!!.payloadCase}")
        mQueue.add(PendingMessage(false, response))
        pumpMessages()
    }

    private fun pumpMessages() = runGuardedOnLooper(mPumpPending) {
        Log.d(TAG, "pumpMessages():")
        while (mQueue.isNotEmpty()) {
            val pending = mQueue.poll()!!

            if (pending.isRequest) {
                mWaitingForResponse[pending.id] = pending
            }

            val outgoing = mMessenger.send(pending.message)
            pending.setOutgoingMessage(outgoing)
        }
    }

    private fun timeoutSession(): Unit = runOnLooper(timeout = HEARTBEAT_MS, enforcePost = true) {
        if (state != ConnectionState.CONNECTED) {
            return@runOnLooper
        }

        val duration = System.currentTimeMillis() - mLastTransferMs
        Log.d(TAG, "timeoutSession(): state=$state duration=$duration")

        if (duration > TIMEOUT_MS) {
            Log.e(TAG, "timeout(): Session timeout")
            reconnect()
            return@runOnLooper
        }

        timeoutSession()

        if (duration < HEARTBEAT_MS || mPostedHeartbeat?.isDone == false) {
            return@runOnLooper
        }

        mPostedHeartbeat = request(
            GenericMessage.newBuilder()
                .setHeartbeat(Empty.getDefaultInstance())
                .build()
        )
    }

    private fun updateLastTransfer() = runOnLooper {
        val currentTimeMs = System.currentTimeMillis()
        Log.d(TAG, "updateLastTransfer(): state=$state duration=${currentTimeMs - mLastTransferMs}")
        mLastTransferMs = currentTimeMs
        timeoutSession()
    }

    private inner class PendingMessage(
        val isRequest: Boolean,
        val message: GenericMessage,
    ) {
        val future = CompletableFuture<GenericMessage>()
        private var mOutgoing: IOutgoingMessage? = null

        val id: Long
            get() = if (isRequest) {
                message.requestId
            } else {
                message.responseId
            }

        fun setOutgoingMessage(outgoing: IOutgoingMessage) {
            assert(mOutgoing == null)
            mOutgoing = outgoing

            outgoing.response.whenComplete(this::onResponse)
        }

        fun onResponse(_outgoing: IOutgoingMessage?, exception: Throwable?) {
            Log.d(TAG, "onResponse(): exception=$exception")
            if (exception != null) {
                updateLastTransfer()
            }
        }
    }

    override fun toString(): String = "Session[$mMessenger]"

    private inner class Client(
        private val mUserClient: ISessionClient
    ) : IMessageClient {

        private fun onResponse(response: GenericMessage) = assertLooper {
            Log.d(TAG, "onResponse(): id = ${response.responseId}")
            val pendingMessage = mWaitingForResponse.remove(response.responseId)
            if (pendingMessage == null) {
                Log.w(TAG, "Received a response for non existing request id=${response.responseId}")
                return@assertLooper
            }
            Log.d(TAG, "onResponse(): completing request id=${response.responseId}")
            pendingMessage.future.complete(response)
        }

        private fun onRequest(request: GenericMessage) = assertLooper {
            Log.d(TAG, "onRequest(): received request id=${request.requestId}")

            val response = processControlRequests(request)
            if (response != null) {
                Log.d(TAG, "onRequest(): Control request processed")
                onRequestDone(request.requestId, response, null)
                return@assertLooper
            }

            Log.d(TAG, "onRequest(): Deferring request to client")
            runOnLooper(targetHandler = mClientHandler) {
                mUserClient.onRequest(request)
                    .whenComplete { response, exception ->
                        onRequestDone(request.requestId, response, exception)
                    }
            }
        }

        override fun onMessageReceived(message: GenericMessage) = runOnLooper {
            Log.i(TAG, "onMessageReceived(): messagePayloadCase=${message.payloadCase}")

            if (mSessionId != null && message.sessionId != mSessionId) {
                Log.w(TAG, "onMessageReceived(): Received a message invalid session id")
                return@runOnLooper
            }

            updateLastTransfer()
            if (message.hasField(REQUEST_ID_FIELD) && !message.hasField(RESPONSE_ID_FIELD)) {
                onRequest(message)
            } else if (message.hasField(RESPONSE_ID_FIELD) && !message.hasField(REQUEST_ID_FIELD)) {
                onResponse(message)
            } else {
                Log.w(TAG, "onMessageReceived(): Received a message that is not a request nor a response")
            }
        }

        override fun onStateChanged(state: ConnectionState) = runOnLooper {
            if (state == ConnectionState.CONNECTED) {
                updateLastTransfer()
            }
            runOnLooper(targetHandler = mClientHandler) {
                mUserClient.onStateChanged(state)
            }
        }

        override fun onError() =
            runOnLooper(targetHandler = mClientHandler) {
                mUserClient.onError()
            }

    }

    class Builder : AbstractSessionBuilder<Builder>() {
        private var mMessenger: IMessengerProvider? = null

        fun setMessenger(messenger: IMessengerProvider): Builder {
            mMessenger = messenger
            return this
        }

        override fun construct(
            client: ISessionClient,
            handler: Handler,
            clientHandler: Handler
        ): ISession = Session(mMessenger!!, client, handler, clientHandler)
    }

}

