package programatorus.client.shared

import programatorus.client.model.Board
import programatorus.client.model.Firmware
import programatorus.client.model.FirmwareData

class FirmwareService() {
    val repository = FavoritesRepository<Firmware>()

    fun pull() {
        // TODO: MSG Get Firmware files
        val all = (1..5).map { Firmware(it.toString(), true) } + (6..12).map { Firmware(it.toString(), false) }
        val favorites = (1..5).map { Firmware(it.toString(), true) }

        repository.setState(all, favorites)
    }

    fun push() {
        repository.getAll()
        repository.getFavorites()

        // TODO: MSG Post Firmware files
    }

    fun getAll(): List<Firmware> = repository.getAll()

    fun getFirmwareData(): FirmwareData = FirmwareData(repository.getAll(), repository.getFavorites())
}