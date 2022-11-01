package programatorus.client.screens.boards.all

import java.util.concurrent.atomic.AtomicBoolean

data class AllBoardsListItem(val name: String, var isSelected: AtomicBoolean = AtomicBoolean(false)){

    fun toggle(): Unit {
        var temp: Boolean
        do {
            temp = isSelected.get()
        } while (!isSelected.compareAndSet(temp, !temp))
    }

    companion object {
        fun from(name: String, value: Boolean): AllBoardsListItem =
            AllBoardsListItem(name, AtomicBoolean(value))
    }
}


