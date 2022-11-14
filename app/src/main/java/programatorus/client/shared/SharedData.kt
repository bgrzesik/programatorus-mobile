package programatorus.client

import programatorus.client.model.Board
import programatorus.client.shared.ConfigurationsManager

object SharedData {

    val boardManager = ConfigurationsManager<Board>(
        (1..5).map { Board(it.toString(), true) } + (6..12).map { Board(it.toString(), false) },
//        listOf()
        (1..5).map { Board(it.toString(), true) }
    )
}