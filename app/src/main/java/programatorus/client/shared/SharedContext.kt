package programatorus.client

import programatorus.client.device.IDevice
import programatorus.client.shared.BoardsService
import programatorus.client.shared.FirmwareService
import java.util.concurrent.CompletableFuture

object SharedContext {

    fun start(device: IDevice): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        if (!isInitialized) {
            deviceClient = device
            getFirmware().thenRun {
                getBoards().thenRun { future.complete(true) }
            }
        }
        isInitialized = true
        return future
    }

    var isInitialized: Boolean = false

    lateinit var deviceClient: IDevice

    fun getBoardsBlocking() {
        deviceClient.getBoards().get().also { boards ->
            boardsService.repository.setState(boards.all, boards.favorites)
        }
    }

    fun getFirmwareBlocking() {
        deviceClient.getFirmware().get().also { firmware ->
            firmwareService.repository.setState(firmware.all, firmware.favorites)
        }
    }

    fun getBoards(): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        deviceClient.getBoards().thenAccept { boards ->
            boardsService.repository.setState(boards.all, boards.favorites)
            future.complete(true)
        }
        return future
    }

    fun getFirmware(): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        deviceClient.getFirmware().thenAccept { firmware ->
            firmwareService.repository.setState(firmware.all, firmware.favorites)
            future.complete(true)
        }
        return future
    }

    val boardsService = BoardsService()
    val firmwareService = FirmwareService()

}