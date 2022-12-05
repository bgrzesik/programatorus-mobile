package programatorus.client.model

data class Firmware(val name: String, val isFavorite: Boolean)

data class FirmwareData(val all: List<Firmware>, val favorites: List<Firmware>)