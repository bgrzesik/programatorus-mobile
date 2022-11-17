package programatorus.client.screens.firmware.all

import programatorus.client.model.Firmware
import java.util.concurrent.atomic.AtomicBoolean

data class AllFirmwaresListItem(val name: String, var isSelected: AtomicBoolean = AtomicBoolean(false)){

    fun toggle() {
        var temp: Boolean
        do {
            temp = isSelected.get()
        } while (!isSelected.compareAndSet(temp, !temp))
    }

    fun isFavorite(): Boolean = isSelected.get()


    companion object {
        fun from(name: String, value: Boolean): AllFirmwaresListItem =
            AllFirmwaresListItem(name, AtomicBoolean(value))

        fun from(firmware: Firmware): AllFirmwaresListItem =
            AllFirmwaresListItem(firmware.name, AtomicBoolean(firmware.isFavorite))
    }

    fun asFirmware(): Firmware = Firmware(name, isFavorite())
}


