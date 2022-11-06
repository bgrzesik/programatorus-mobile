package programatorus.client.shared

class ConfigurationsManager<T>(
    private var allItems: List<T> = listOf(),
    private var favItems: List<T> = listOf()
) {


    fun updateFavorites(boards: List<T>) {
        allItems=boards

        val favorites = favItems.toSet()
        favItems = favItems + boards.filter { !favorites.contains(it) }
    }

    fun updateOrder(boards: List<T>) {
        favItems = boards as MutableList<T>
    }

    fun getAll(): List<T> = allItems

    fun getFavorites(): List<T> = favItems

}