package programatorus.client.screens.choosedevice

import android.bluetooth.BluetoothDevice

class ChooseDeviceContract {

    interface View {
        fun setPairedDevices(bondedDevices: MutableSet<BluetoothDevice>)
    }

    interface Presenter {
        fun start()
    }


}