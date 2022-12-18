package programatorus.client.device

import programatorus.client.model.Board
import android.net.Uri
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.model.BoardsData
import programatorus.client.model.Firmware
import programatorus.client.model.FirmwareData
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

interface IDevice {

    val isConnected: Boolean

    val onDisconnect: CompletableFuture<ConnectionState>

    fun getBoards() : CompletableFuture<BoardsData>

    fun getFirmware() : CompletableFuture<FirmwareData>

    fun putBoards(data: BoardsData): CompletableFuture<Boolean>

    fun putFirmware(data: FirmwareData): CompletableFuture<Boolean>

    // TODO(bgrzesik): add progress
    fun upload(documentUri: Uri) : CompletableFuture<Unit>

    fun flashRequest(board: Board, firmware: Firmware): CompletableFuture<String>

    fun startDebugger(board: Board, firmware: Firmware): CompletableFuture<Int>

    fun stopDebugger(): CompletableFuture<Unit>

    fun sendDebuggerLine(line: String): CompletableFuture<Unit>

    fun pollDebuggerLine(): CompletableFuture<String>

    fun deleteFirmware(name: String): CompletableFuture<Unit>

}