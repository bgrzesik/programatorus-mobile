package programatorus.client.device

import android.net.Uri
import programatorus.client.model.Board
import java.util.concurrent.CompletableFuture

interface IDevice {

    val isConnected: Boolean

    fun getBoards() : CompletableFuture<List<Board>>

    // TODO(bgrzesik): add progress
    fun upload(documentUri: Uri) : CompletableFuture<Unit>

}