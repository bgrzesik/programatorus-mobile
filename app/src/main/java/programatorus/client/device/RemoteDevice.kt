package programatorus.client.device

import android.content.Context
import android.net.Uri
import android.os.Handler
import programatorus.client.comm.IConnectionClient
import programatorus.client.comm.app.RequestRouter
import programatorus.client.comm.app.proto.FileUpload
import programatorus.client.comm.app.proto.GetBoards
import programatorus.client.comm.app.proto.GetFirmware
import programatorus.client.comm.app.proto.PutBoards
import programatorus.client.comm.app.proto.PutFirmware
import programatorus.client.comm.app.proto.*
import programatorus.client.comm.session.ISession
import programatorus.client.comm.session.ISessionProvider
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.model.Board
import programatorus.client.model.BoardsData
import programatorus.client.model.Firmware
import programatorus.client.model.FirmwareData
import programatorus.client.utils.HandlerTaskRunner
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class RemoteDevice(
        sessionBuilder: ISessionProvider,
        handler: Handler,
        private val mContext: Context,
) : IDevice, IConnectionClient {

    private val mDebuggerLinesQueue = DebuggerLineQueue()

    private val mRouter: RequestRouter = RequestRouter(
            listOf(
                mDebuggerLinesQueue
            ),
            this
    )

    private val mTaskRunner = HandlerTaskRunner(handler)
    private val mSession: ISession = sessionBuilder.build(mRouter, mTaskRunner, mTaskRunner)
    private val mOnDisconnect = CompletableFuture<ConnectionState>()

    init {
        mSession.reconnect()
    }

    override fun onStateChanged(state: ConnectionState) {
        if (state == ConnectionState.DISCONNECTED || state == ConnectionState.ERROR) {
            mOnDisconnect.complete(state)
        }
    }

    override val isConnected: Boolean
        get() = mSession.state == ConnectionState.CONNECTING || mSession.state == ConnectionState.CONNECTED

    override val onDisconnect: CompletableFuture<ConnectionState>
        get() = mOnDisconnect

    override fun getBoards() = GetBoards().request(mSession)

    override fun getFirmware() = GetFirmware().request(mSession)

    override fun putBoards(data: BoardsData) = PutBoards(data).request(mSession)

    override fun putFirmware(data: FirmwareData) = PutFirmware(data).request(mSession)

    override fun upload(documentUri: Uri): CompletableFuture<Unit> =
        FileUpload.upload(mSession, documentUri, mContext.contentResolver)

    override fun flashRequest(board: Board, firmware: Firmware) = FlashRequest(board, firmware).request(mSession)

    override fun startDebugger(board: Board, firmware: Firmware) = StartDebugger(board, firmware).request(mSession)

    override fun stopDebugger() = StopDebugger().request(mSession)

    override fun sendDebuggerLine(line: String) = SendDebuggerLine(line).request(mSession)

    override fun pollDebuggerLine() = mDebuggerLinesQueue.poll()

    override fun deleteFirmware(name: String) = DeleteFirmware(name).request(mSession)

}