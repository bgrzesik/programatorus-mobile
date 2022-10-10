package programatorus.client.transport

import android.util.Log

abstract class AbstractTransport(
    protected val client: ITransportClient,
    private var mState: ConnectionState = ConnectionState.DISCONNECTED
) : ITransport {
    companion object {
        private const val TAG = "AbstractTransport"
    }

    override var state: ConnectionState
        get() = mState
        set(value) {
            if (mState != value) {
                Log.d(TAG, "Changing transport state $mState -> $value")
                mState = value
                client.onStateChanged(mState)
            }
        }
}