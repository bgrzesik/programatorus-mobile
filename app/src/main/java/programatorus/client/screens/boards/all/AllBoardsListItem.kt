package programatorus.client.screens.boards.all

import programatorus.client.model.Board
import java.util.concurrent.atomic.AtomicBoolean

data class AllBoardsListItem(val name: String, var isSelected: AtomicBoolean = AtomicBoolean(false)){

    fun toggle() {
        var temp: Boolean
        do {
            temp = isSelected.get()
        } while (!isSelected.compareAndSet(temp, !temp))
    }

    fun isFavorite(): Boolean = isSelected.get()


    companion object {
        fun from(name: String, value: Boolean): AllBoardsListItem =
            AllBoardsListItem(name, AtomicBoolean(value))

        fun from(board: Board): AllBoardsListItem =
            AllBoardsListItem(board.name, AtomicBoolean(board.isFavorite))
    }

    fun asBoard(): Board = Board(name, isFavorite())
}


