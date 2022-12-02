package programatorus.client.device

import android.net.Uri
import programatorus.client.model.Board
import programatorus.client.model.BoardsData
import programatorus.client.model.Firmware
import programatorus.client.model.FirmwareData
import java.util.concurrent.CompletableFuture

interface DeviceWrapper: IDevice {

    val wrappedDevice: IDevice

    override val isConnected: Boolean
        get() = wrappedDevice.isConnected

    override fun getBoards() = wrappedDevice.getBoards()

    override fun getFirmware() = wrappedDevice.getFirmware()

    override fun putBoards(data: BoardsData) = wrappedDevice.putBoards(data)

    override fun putFirmware(data: FirmwareData) = wrappedDevice.putFirmware(data)

    override fun upload(documentUri: Uri) = wrappedDevice.upload(documentUri)

    override fun flashRequest(board: Board, firmware: Firmware) = wrappedDevice.flashRequest(board, firmware)

}