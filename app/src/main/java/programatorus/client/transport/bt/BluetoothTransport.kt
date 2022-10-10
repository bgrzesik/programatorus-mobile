package programatorus.client.transport.bt

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import programatorus.client.WeakRefFactoryMixin
import programatorus.client.transport.ITransportClient
import programatorus.client.transport.io.IOTransport
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothTransport(
    private val mContext: Context,
    private val mDevice: BluetoothDevice,
    client: ITransportClient,
    handler: Handler = Handler(Looper.getMainLooper())
) : IOTransport<BluetoothTransport>(client, handler),
    WeakRefFactoryMixin<BluetoothTransport> {

    companion object {
        private const val TAG = "BluetoothTransport"
        private val BT_SERVICE_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private var mSocket: BluetoothSocket? = null

    override var inputStream: InputStream? = null
        private set

    override var outputStream: OutputStream? = null
        private set

    override val isConnected: Boolean
        get() = (mSocket != null && inputStream != null && outputStream != null) && mSocket!!.isConnected


    override fun doConnect(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
                mContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "No permissions")
            return false
        }

        try {
            Log.i(TAG, "Creating BT socket")
            mSocket = mDevice.createRfcommSocketToServiceRecord(BT_SERVICE_UUID)
            inputStream = mSocket?.inputStream
            outputStream = mSocket?.outputStream
            mSocket?.connect()

            Log.i(TAG, "Created BT socket")
        } catch (ex: IOException) {
            Log.e(TAG, "Connection failed", ex)
        }

        Log.e(TAG, "isConnected=${isConnected}")
        return isConnected
    }

    override fun doDisconnect() {
        inputStream?.close()
        inputStream = null

        outputStream?.close()
        outputStream = null
    }

}