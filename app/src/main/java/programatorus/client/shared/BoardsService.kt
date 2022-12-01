package programatorus.client.shared

import programatorus.client.SharedContext
import programatorus.client.model.Board
import programatorus.client.model.BoardsData

class BoardsService() {
    val repository = FavoritesRepository<Board>()

    fun pull() {
        SharedContext.deviceClient.getBoards().thenAccept { boards ->
            repository.setState(boards.all, boards.favorites)
        }
    }

    fun push() {
        repository.getAll()
        repository.getFavorites()

        // TODO: MSG Post boards
    }

    fun getAll(): List<Board> = repository.getAll()
}