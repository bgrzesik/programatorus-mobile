package programatorus.client

import programatorus.client.device.IDevice
import programatorus.client.shared.BoardsService
import programatorus.client.shared.FirmwareService
import programatorus.client.shared.FlashService
import java.util.concurrent.CompletableFuture

object RemoteContext {

    fun start(device: IDevice): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        if (!isInitialized) {
            initServices(device)
            fetchInitialConfig(future)
        } else {
            future.complete(true)
        }
        isInitialized = true
        return future
    }

    private fun initServices(device: IDevice) {
        boardsService.client = device
        firmwareService.client = device
        flashService.client = device
    }

    private fun fetchInitialConfig(future: CompletableFuture<Boolean>) {
        boardsService.pull().thenRun {
            firmwareService.pull().thenRun {
                future.complete(true)
            }
        }
    }

    var isInitialized: Boolean = false

    val boardsService = BoardsService()
    val firmwareService = FirmwareService()
    val flashService = FlashService()

}