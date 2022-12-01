package programatorus.client.model

import programatorus.client.shared.Likeable

data class Board(val name: String, override val isFavorite: Boolean): Likeable

data class BoardsData(val all: List<Board>, val favorites: List<Board>)