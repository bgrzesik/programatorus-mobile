package programatorus.client.model

import programatorus.client.shared.Likeable

data class Firmware(val name: String, override val isFavorite: Boolean): Likeable

data class FirmwareData(val all: List<Firmware>, val favorites: List<Firmware>)