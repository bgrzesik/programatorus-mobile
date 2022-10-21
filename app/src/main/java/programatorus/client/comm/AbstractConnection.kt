package programatorus.client.comm

import android.util.Log
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.comm.transport.ITransportClient

abstract class AbstractConnection(
    protected val client: ITransportClient,
    private var mState: ConnectionState = ConnectionState.DISCONNECTED
) : IConnection {
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