package programatorus.client.device

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import programatorus.client.comm.presentation.IMessengerProvider
import programatorus.client.comm.presentation.Messenger
import programatorus.client.comm.presentation.ProtocolMessenger
import programatorus.client.comm.session.Session
import programatorus.client.comm.transport.ITransportProvider
import programatorus.client.comm.transport.bt.BluetoothTransport
import programatorus.client.comm.transport.net.TcpTransport
import programatorus.client.comm.transport.wrapper.Transport

class DeviceBuilder {

    private var mWrapTransport: Boolean = false
    private var mWrapMessenger: Boolean = false

    private var mTransportProvider: ITransportProvider? = null


    fun bluetooth(context: Context, bluetoothDevice: BluetoothDevice): DeviceBuilder {
        mTransportProvider = BluetoothTransport.Builder()
                .setContext(context)
                .setDevice(bluetoothDevice)
        return this
    }

    fun tcp(address: String, port: Int): DeviceBuilder {
        mTransportProvider = TcpTransport.Builder()
                .setHost(address)
                .setPort(port)
        return this
    }

    fun wrapTransport() {
        mWrapTransport = true
    }

    fun dontWrapTransport() {
        mWrapTransport = false
    }

    fun wrapMessenger() {
        mWrapMessenger = true
    }

    fun dontWrapMessenger() {
        mWrapMessenger = false
    }

    fun build(handler: Handler) : IDevice {
        var transportProvider = mTransportProvider!!

        if (mWrapTransport) {
            transportProvider = Transport.Builder()
                    .setTransport(transportProvider)
        }

        var messenger: IMessengerProvider = ProtocolMessenger.Builder()
                .setTransport(transportProvider)

        if (mWrapMessenger) {
            messenger = Messenger.Builder()
                    .setMessenger(messenger)
        }

        val session = Session.Builder()
                .setMessenger(messenger)

        return RemoteDevice(session, handler)
    }

}