package programatorus.client.shared

import programatorus.client.model.Board

class BoardsService() {
    val repository = FavoritesRepository<Board>()

    fun pull() {
        // TODO: MSG Get boards
        val all = (1..5).map { Board(it.toString(), true) } + (6..12).map { Board(it.toString(), false) }
        val favorites = (1..5).map { Board(it.toString(), true) }

        repository.setState(all, favorites)
    }

    fun push() {
        repository.getAll()
        repository.getFavorites()

        // TODO: MSG Post boards
    }
}