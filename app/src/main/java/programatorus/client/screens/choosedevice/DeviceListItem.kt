package programatorus.client.screens.choosedevice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

data class DeviceListItem(
    val name: String,
    val address: String
) {
    companion object {
        @SuppressLint("MissingPermission")
        fun from(bluetoothDevice: BluetoothDevice) =
            DeviceListItem(bluetoothDevice.name, bluetoothDevice.address)
    }
}
