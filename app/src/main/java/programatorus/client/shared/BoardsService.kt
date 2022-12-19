package programatorus.client.shared

import programatorus.client.device.IDevice
import programatorus.client.model.Board
import programatorus.client.model.BoardsData
import java.util.concurrent.CompletableFuture

class BoardsService() {
    val repository = FavoritesRepository<Board>()
    lateinit var client: IDevice

    fun pull(): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()
        client.getBoards().thenAccept { boards ->
            repository.setState(boards.all, boards.favorites)
            future.complete(Unit)
        }
        return future
    }

    fun push(): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        client.putBoards(getBoardsData())
            .thenAccept { future.complete(it) }
        return future
    }

    fun getAll(): List<Board> = repository.getAll()

    fun getAllSorted(): List<Board> =
        repository.getFavorites() + repository.getAll().filter { !it.isFavorite }

    fun getBoardsData(): BoardsData = BoardsData(repository.getAll(), repository.getFavorites())
}