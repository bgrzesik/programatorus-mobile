package programatorus.client.device

import programatorus.client.comm.app.proto.OnDebuggerLine
import java.util.concurrent.CompletableFuture
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class DebuggerLineQueue : OnDebuggerLine {

    private val mLock = ReentrantLock()
    private val mQueue = ArrayDeque<String>()
    private var mCurrent: CompletableFuture<String>? = null

    override fun onRequest(request: String): CompletableFuture<Unit> {
        var pendingCurrent: CompletableFuture<String>? = null

        mLock.withLock {
            if (mCurrent != null) {
                pendingCurrent = mCurrent
                mCurrent = null
            } else {
                mQueue.add(request)
            }
        }

        pendingCurrent?.complete(request)

        val fut = CompletableFuture<Unit>()
        fut.complete(Unit)
        return fut
    }

    fun poll(): CompletableFuture<String> =
        mLock.withLock {
            if (mCurrent != null) {
                return mCurrent!!
            } else if (mQueue.isEmpty()) {
                mCurrent = CompletableFuture()
                return mCurrent!!
            } else {
                val fut = CompletableFuture<String>()
                fut.complete(mQueue.removeFirst())
                return fut
            }
        }

}