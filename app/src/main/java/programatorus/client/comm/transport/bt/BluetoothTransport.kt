package programatorus.client.comm.transport.bt

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import programatorus.client.comm.transport.AbstractTransportBuilder
import programatorus.client.comm.transport.ITransport
import programatorus.client.comm.transport.ITransportClient
import programatorus.client.comm.transport.io.StreamingTransport
import programatorus.client.utils.TaskRunner
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothTransport(
    private val mContext: Context,
    private val mDevice: BluetoothDevice,
    client: ITransportClient,
) : StreamingTransport<BluetoothTransport>(client) {

    companion object {
        private const val TAG = "BluetoothTransport"
        private val BT_SERVICE_UUID: UUID = UUID.fromString("0446eb5c-d775-11ec-9d64-0242ac120002")
    }

    private var mSocket: BluetoothSocket? = null

    override var inputStream: InputStream? = null
        private set
    override var outputStream: OutputStream? = null
        private set

    override val isConnected: Boolean
        get() = (mSocket != null && inputStream != null && outputStream != null) && mSocket!!.isConnected

    override val disconnectOnReconnect: Boolean
        get() = true

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

    override fun toString(): String = "BluetoothTransport"

    class Builder : AbstractTransportBuilder<Builder>() {
        private var mContext: Context? = null
        private var mDevice: BluetoothDevice? = null

        fun setContext(context: Context): Builder {
            mContext = context
            return this
        }

        fun setDevice(device: BluetoothDevice): Builder {
            mDevice = device
            return this
        }

        override fun construct(
            client: ITransportClient,
            taskRunner: TaskRunner,
            clientTaskRunner: TaskRunner
        ): ITransport = BluetoothTransport(mContext!!, mDevice!!, client)
    }
}