package programatorus.client.device

import android.os.Handler
import programatorus.client.comm.app.RequestRouter
import programatorus.client.comm.app.proto.GetBoards
import programatorus.client.comm.session.ISession
import programatorus.client.comm.session.ISessionProvider
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.utils.HandlerTaskRunner

class RemoteDevice(
        sessionBuilder: ISessionProvider,
        handler: Handler,
) : IDevice {

    private val mRouter: RequestRouter = RequestRouter(
            emptyList()
    )

    private val mTaskRunner = HandlerTaskRunner(handler)
    private val mSession: ISession = sessionBuilder.build(mRouter, mTaskRunner, mTaskRunner)

    init {
        // TODO(bgrzesik): Proper connection handling
        mSession.reconnect()
    }

    override val isConnected: Boolean
        get() = mSession.state == ConnectionState.CONNECTING || mSession.state == ConnectionState.CONNECTED

    override fun getBoards() = GetBoards().request(mSession)

}