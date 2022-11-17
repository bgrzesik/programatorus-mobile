package programatorus.client

import programatorus.client.shared.BoardsService
import programatorus.client.shared.FirmwareService

object SharedContext {

//    val boardManager = FavoritesManager<Board>(
//        (1..5).map { Board(it.toString(), true) } + (6..12).map { Board(it.toString(), false) },
////        listOf()
//        (1..5).map { Board(it.toString(), true) }
//    )

    val boardsService = BoardsService()
    val firmwareService = FirmwareService()

}