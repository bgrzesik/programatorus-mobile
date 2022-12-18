package programatorus.client.device

import android.net.Uri
import programatorus.client.comm.transport.ConnectionState
import programatorus.client.model.Board
import programatorus.client.model.BoardsData
import programatorus.client.model.Firmware
import programatorus.client.model.FirmwareData
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

interface DeviceWrapper: IDevice {

    val wrappedDevice: IDevice

    override val isConnected: Boolean
        get() = wrappedDevice.isConnected

    override val onDisconnect: CompletableFuture<ConnectionState>
        get() = wrappedDevice.onDisconnect

    override fun getBoards() = wrappedDevice.getBoards()

    override fun getFirmware() = wrappedDevice.getFirmware()

    override fun putBoards(data: BoardsData) = wrappedDevice.putBoards(data)

    override fun putFirmware(data: FirmwareData) = wrappedDevice.putFirmware(data)

    override fun upload(documentUri: Uri) = wrappedDevice.upload(documentUri)

    override fun flashRequest(board: Board, firmware: Firmware) = wrappedDevice.flashRequest(board, firmware)

    override fun startDebugger(board: Board, firmware: Firmware) = wrappedDevice.startDebugger(board, firmware)

    override fun stopDebugger() = wrappedDevice.stopDebugger()

    override fun sendDebuggerLine(line: String) = wrappedDevice.sendDebuggerLine(line)

    override fun pollDebuggerLine() = wrappedDevice.pollDebuggerLine()

    override fun deleteFirmware(name: String) = wrappedDevice.deleteFirmware(name)

}