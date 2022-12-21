package programatorus.client

import programatorus.client.device.IDevice
import programatorus.client.shared.BoardsService
import programatorus.client.shared.FirmwareService
import programatorus.client.shared.FlashService
import java.util.concurrent.CompletableFuture

object RemoteContext {

    var isInitialized: Boolean = false
    var fetchNeeded: Boolean = true
    lateinit var device: IDevice

    val boardsService = BoardsService()
    val firmwareService = FirmwareService()
    val flashService = FlashService()

    fun start(device: IDevice): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        if (!isInitialized) {
            this.device = device
            initServices(device)
        }
        isInitialized = true

        if (fetchNeeded)
            fetch(future)
        else
            future.complete(true)
        return future
    }

    private fun initServices(device: IDevice) {
        boardsService.client = device
        firmwareService.client = device
        flashService.client = device
    }

    private fun fetch(future: CompletableFuture<Boolean>) {
        boardsService.pull().thenRun {
            firmwareService.pull().thenRun {
                future.complete(true)
            }
        }
        fetchNeeded = false
    }


}