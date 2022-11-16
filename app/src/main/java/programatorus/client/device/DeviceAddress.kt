package programatorus.client.device

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

sealed class DeviceAddress : Parcelable {

    @Parcelize
    data class TcpDevice(val address: String, val port: Int) : DeviceAddress()

    @Parcelize
    data class BluetoothDevice(val mac: String) : DeviceAddress()

}
