package programatorus.client.screens.firmware.favorites

import programatorus.client.model.Firmware

data class FavFirmwaresListItem(
    val name: String,
) {

    companion object {
        fun from(firmware: Firmware): FavFirmwaresListItem =
            FavFirmwaresListItem(firmware.name)
    }

    fun asFirmware(): Firmware = Firmware(name, true)
}
