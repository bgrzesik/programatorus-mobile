package programatorus.client.comm.session

import android.util.Log
import com.google.protobuf.Empty
import programatorus.client.comm.presentation.IMessageClient
import programatorus.client.comm.presentation.IMessengerProvider
import programatorus.client.comm.presentation.IOutgoingMessage
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.utils.ActiveObject
import programatorus.client.utils.TaskRunner
import programus.proto.Protocol
import programus.proto.Protocol.GenericMessage
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class Session private constructor(
    messenger: IMessengerProvider,
    client: ISessionClient,
    private val mTaskRunner: TaskRunner,
    private val mClientTaskRunner: TaskRunner
) : ISession, ActiveObject {

    private companion object {
        const val TAG = "Session"

        const val HEARTBEAT_MS: Long = 2000
        const val TIMEOUT_MS: Long = 8 * HEARTBEAT_MS
    }


    private val mTimeoutGuard = AtomicBoolean(false)
    private var mSessionId: Long? = null
    private var mLastTransferMs = System.currentTimeMillis()
    private var mLastHeartbeatMs = System.currentTimeMillis()
    private var mPostedHeartbeat: CompletableFuture<GenericMessage>? = null
    private val mNextRequestId = AtomicLong(0)
    private val mPumpPending = AtomicBoolean(false)

    private val mWaitingForResponse = mutableMapOf<Long, PendingMessage>()

    private val mClient = Client(client)
    private val mQueue = LinkedBlockingQueue<PendingMessage>()
    private val mMessenger = messenger.build(mClient, mTaskRunner, mClientTaskRunner)

    override val taskRunner: TaskRunner
        get() = mTaskRunner

    override fun request(message: GenericMessage): CompletableFuture<GenericMessage> {
        Log.d(TAG, "request(): ${message.payloadCase}")

        val pending = PendingMessage(
            GenericMessage.newBuilder(message)
                .apply {
                    if (mSessionId != null) {
                        sessionId = mSessionId!!
                    }
                    request = mNextRequestId.incrementAndGet()
                }
                .build()
        )

        mQueue.add(pending)
        pumpMessages()
        return pending.future
    }

    override val state: ConnectionState
        get() = mMessenger.state

    override fun reconnect() = runOnLooper {
        mPostedHeartbeat?.cancel(true)
        mPostedHeartbeat = null
        mMessenger.reconnect()
    }

    override fun disconnect() = runOnLooper {
        mPostedHeartbeat?.cancel(true)
        mPostedHeartbeat = null
        mMessenger.disconnect()
    }

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
                mSessionId = message.setSessionId.sessionId
                Log.i(TAG, "processControlRequests(): Setting sessionId=$mSessionId")

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
            Log.e(TAG, "onRequest(): ", exception)
            response = GenericMessage.newBuilder()
                .setResponse(requestId)
                .apply {
                    if (mSessionId != null) {
                        sessionId = mSessionId!!
                    }
                }
                .setError( // TODO(bgrzesik): proper error mapping
                    Protocol.ErrorMessage.newBuilder()
                        .setDescription(exception.message)
                )
                .build()
        } else {
            response = GenericMessage.newBuilder(response)
                .clearId()
                .setResponse(requestId)
                .apply {
                    if (mSessionId != null) {
                        sessionId = mSessionId!!
                    }
                }
                .build()
        }

        Log.d(
            TAG,
            "onRequestDone(): requestId=$requestId responsePayloadCase=${response!!.payloadCase}"
        )
        mQueue.add(PendingMessage(response))
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

    private fun timeoutSession(): Unit =
        runGuardedOnLooper(mTimeoutGuard, timeout = HEARTBEAT_MS, enforcePost = true) {
            if (state != ConnectionState.CONNECTED) {
                return@runGuardedOnLooper
            }

            val duration = System.currentTimeMillis() - mLastTransferMs
            Log.d(TAG, "timeoutSession(): state=$state duration=$duration")

            if (duration > TIMEOUT_MS) {
                Log.e(TAG, "timeoutSession(): Session timeout")
                disconnect()
                return@runGuardedOnLooper
            }

            timeoutSession()

            if (duration < HEARTBEAT_MS) {
                return@runGuardedOnLooper
            }

            val sinceLastHeartbeat = System.currentTimeMillis() - mLastHeartbeatMs
            if (sinceLastHeartbeat <= HEARTBEAT_MS && mPostedHeartbeat != null && mPostedHeartbeat?.isDone == false) {
                return@runGuardedOnLooper
            }

            Log.d(TAG, "timeoutSession(): Sending heartbeat")
            mPostedHeartbeat = request(
                GenericMessage.newBuilder()
                    .setHeartbeat(Empty.getDefaultInstance())
                    .build()
            )
            mLastHeartbeatMs = System.currentTimeMillis()
        }

    private fun updateLastTransfer() = runOnLooper {
        val currentTimeMs = System.currentTimeMillis()
        Log.d(TAG, "updateLastTransfer(): state=$state duration=${currentTimeMs - mLastTransferMs}")
        mLastTransferMs = currentTimeMs
        timeoutSession()
    }

    private inner class PendingMessage(
        val message: GenericMessage,
    ) {
        val future = CompletableFuture<GenericMessage>()
        private var mOutgoing: IOutgoingMessage? = null

        val isRequest: Boolean
            get() = message.idCase == GenericMessage.IdCase.REQUEST

        val id: Long
            get() = if (isRequest) {
                message.request
            } else {
                message.response
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
            Log.d(TAG, "onResponse(): id=${response.response}, Type=${response.payloadCase}")
            assert(response.hasResponse())
            val pendingMessage = mWaitingForResponse.remove(response.response)
            if (pendingMessage == null) {
                Log.w(
                    TAG,
                    "onResponse(): Received a response for non existing request id=${response.response}"
                )
                return@assertLooper
            }
            Log.d(TAG, "onResponse(): Completing request id=${response.response}")
            // TODO(bgrzesik): Ensure proper order.
            pendingMessage.future.complete(response)
        }

        private fun onRequest(request: GenericMessage) = assertLooper {
            Log.d(TAG, "onRequest(): Received request id=${request.request}")
            assert(request.hasRequest())

            val response = processControlRequests(request)
            if (response != null) {
                Log.d(TAG, "onRequest(): Control request processed")
                onRequestDone(request.request, response, null)
                return@assertLooper
            }

            Log.d(TAG, "onRequest(): Deferring request to client")
            runOnLooper(target = mClientTaskRunner) {
                mUserClient.onRequest(request)
                    .whenComplete { response, exception ->
                        onRequestDone(request.request, response, exception)
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
            when (message.idCase) {
                GenericMessage.IdCase.REQUEST -> onRequest(message)
                GenericMessage.IdCase.RESPONSE -> onResponse(message)
                else ->
                    Log.w(
                        TAG,
                        "onMessageReceived(): Received a message that is not a request nor a response"
                    )
            }
        }

        override fun onStateChanged(state: ConnectionState) = runOnLooper {
            if (state == ConnectionState.CONNECTED) {
                updateLastTransfer()
            }
            runOnLooper(target = mClientTaskRunner) {
                mUserClient.onStateChanged(state)
            }
        }

        override fun onError() =
            runOnLooper(target = mClientTaskRunner) {
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
            taskRunner: TaskRunner,
            clientTaskRunner: TaskRunner
        ): ISession = Session(mMessenger!!, client, taskRunner, clientTaskRunner)
    }

}

