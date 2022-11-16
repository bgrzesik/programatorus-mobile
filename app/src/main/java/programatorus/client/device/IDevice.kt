package programatorus.client.device

import programatorus.client.model.Board
import java.util.concurrent.CompletableFuture

interface IDevice {

    val isConnected: Boolean

    fun getBoards() : CompletableFuture<List<Board>>

}