package programatorus.client.device

import android.util.Log
import programatorus.client.comm.app.proto.OnDebuggerLine
import java.util.concurrent.CompletableFuture
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class DebuggerLineQueue : OnDebuggerLine {
    companion object {
        private const val TAG = "DebuggerLineQueue"
    }

    private val mLock = ReentrantLock()
    private val mQueue = ArrayDeque<String>()
    private var mCurrent: CompletableFuture<String>? = null

    override fun onRequest(request: String): CompletableFuture<Unit> {
        Log.d(TAG, "onRequest(): line=$request")
        var pendingCurrent: CompletableFuture<String>? = null

        mLock.withLock {
            if (mCurrent != null) {
                Log.d(TAG, "poll(): Will notify currently waiting")
                pendingCurrent = mCurrent
                mCurrent = null
            } else {
                Log.d(TAG, "poll(): Adding to queue")
                mQueue.add(request)
            }
        }

        if (pendingCurrent != null) {
            Log.d(TAG, "poll(): Notifying currently pending")
            pendingCurrent!!.complete(request)
        }

        val fut = CompletableFuture<Unit>()
        fut.complete(Unit)
        return fut
    }

    fun poll(): CompletableFuture<String> =
        mLock.withLock {
            if (mCurrent != null) {
                Log.d(TAG, "poll(): Still waiting for line")
                return mCurrent!!
            } else if (mQueue.isEmpty()) {
                Log.d(TAG, "poll(): No more lines in queue creating waiting future")
                mCurrent = CompletableFuture()
                return mCurrent!!
            } else {
                Log.d(TAG, "poll(): There is a lines in a queue ${mQueue.size}")
                val fut = CompletableFuture<String>()
                fut.complete(mQueue.removeFirst())
                return fut
            }
        }

}