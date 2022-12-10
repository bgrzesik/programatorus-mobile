package programatorus.client.device

import programatorus.client.model.Board
import android.net.Uri
import programatorus.client.model.BoardsData
import programatorus.client.model.Firmware
import programatorus.client.model.FirmwareData
import java.util.concurrent.CompletableFuture

interface IDevice {

    val isConnected: Boolean

    fun getBoards() : CompletableFuture<BoardsData>

    fun getFirmware() : CompletableFuture<FirmwareData>

    fun putBoards(data: BoardsData): CompletableFuture<Boolean>

    fun putFirmware(data: FirmwareData): CompletableFuture<Boolean>

    // TODO(bgrzesik): add progress
    fun upload(documentUri: Uri) : CompletableFuture<Unit>

    fun flashRequest(board: Board, firmware: Firmware): CompletableFuture<String>
}