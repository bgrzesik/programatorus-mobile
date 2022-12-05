package programatorus.client.shared

import programatorus.client.device.IDevice
import programatorus.client.model.Firmware
import programatorus.client.model.FirmwareData
import java.util.concurrent.CompletableFuture

class FirmwareService() {
    val repository = FavoritesRepository<Firmware>()
    lateinit var client: IDevice

    fun pull(): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()
        client.getFirmware().thenAccept { firmware ->
            repository.setState(firmware.all, firmware.favorites)
            future.complete(Unit)
        }
        return future
    }

    fun push(): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        client.putFirmware(getFirmwareData())
            .thenAccept { future.complete(it) }
        return future
    }

    fun getAll(): List<Firmware> = repository.getAll()

    private fun getFirmwareData(): FirmwareData =
        FirmwareData(repository.getAll(), repository.getFavorites())
}