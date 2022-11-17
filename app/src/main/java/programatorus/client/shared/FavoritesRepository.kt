package programatorus.client.shared

class FavoritesRepository<T>(
    private var allItems: List<T> = listOf(),
    private var favItems: List<T> = listOf()
) {


    fun updateState(all: List<T>, favorites: List<T>) {
        allItems = all

        val old = favItems.toSet()
        val new = favorites.toSet()

        favItems = favItems.filter { new.contains(it) } + favorites.filter { !old.contains(it) }
    }

    fun setState(all: List<T>, favorites: List<T>) {
        allItems = all
        favItems = favorites
    }

    fun setOrderedFavorites(boards: List<T>) {
        favItems = boards as MutableList<T>
    }

    fun getAll(): List<T> = allItems

    fun getFavorites(): List<T> = favItems

}