package programatorus.client.device

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.os.HandlerCompat


class DeviceService : Service() {
    companion object {
        const val TAG = "DeviceService"
        const val EXTRA_DEVICE = "device"
    }

    inner class Binder(
            override val wrappedDevice: IDevice
    ) : android.os.Binder(), DeviceWrapper

    private lateinit var mHandlerThread : HandlerThread
    private lateinit var mServiceHandler: Handler
    private var mBinder: Binder? = null

    private fun getBluetoothDevice(mac: String): BluetoothDevice {
        val manager = applicationContext.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = manager.adapter
        return adapter.getRemoteDevice(mac)
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate(): ")
        mHandlerThread = HandlerThread("DeviceService", Process.THREAD_PRIORITY_BACKGROUND)
        mHandlerThread.start()
        mServiceHandler = HandlerCompat.createAsync(mHandlerThread.looper)
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "onBind():")
        val deviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_DEVICE, DeviceAddress::class.java)
        } else {
            intent.getParcelableExtra(EXTRA_DEVICE)
        }

        if (mBinder == null) {
            Log.d(TAG, "onBind(): Creating a DeviceBinder deviceType=$deviceType")
            val device = when (deviceType) {
                is DeviceAddress.BluetoothDevice -> DeviceBuilder()
                        .bluetooth(this@DeviceService, getBluetoothDevice(deviceType.mac))
                        .build(mServiceHandler, this)

                is DeviceAddress.TcpDevice -> DeviceBuilder()
                        .tcp(deviceType.address, deviceType.port)
                        .build(mServiceHandler, this)

                null -> throw NullPointerException("Missing device")
            }

            mBinder = Binder(device)
        }

        return mBinder
    }

}