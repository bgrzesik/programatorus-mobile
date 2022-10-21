package programatorus.client.comm

import programatorus.client.comm.transport.ConnectionState

open class ConnectionClientDelegate(
    private val mClient: IConnectionClient
): IConnectionClient {

    override fun onStateChanged(state: ConnectionState) = mClient.onStateChanged(state)

    override fun onError() = mClient.onError()

}