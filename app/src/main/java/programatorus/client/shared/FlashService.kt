package programatorus.client.shared

import programatorus.client.device.IDevice
import programatorus.client.model.Board
import programatorus.client.model.Firmware
import java.util.concurrent.CompletableFuture

class FlashService {
    lateinit var client: IDevice

    fun sendRequest(board: Board, firmware: Firmware): CompletableFuture<String> {
        val future = CompletableFuture<String>()
        client.flashRequest(board, firmware).thenAccept {
            future.complete(it)
        }
        return future
    }
}
