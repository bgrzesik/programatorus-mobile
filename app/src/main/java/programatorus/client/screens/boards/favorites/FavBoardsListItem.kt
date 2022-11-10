package programatorus.client.screens.boards.favorites

import programatorus.client.model.Board

data class FavBoardsListItem(
    val name: String,
) {

    companion object {
        fun from(board: Board): FavBoardsListItem =
            FavBoardsListItem(board.name)
    }

    fun asBoard(): Board = Board(name, true)
}
