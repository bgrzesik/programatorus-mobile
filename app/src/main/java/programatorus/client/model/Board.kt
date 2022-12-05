package programatorus.client.model

data class Board(val name: String, val isFavorite: Boolean)

data class BoardsData(val all: List<Board>, val favorites: List<Board>)