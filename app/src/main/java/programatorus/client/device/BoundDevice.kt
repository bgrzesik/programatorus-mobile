package programatorus.client.device

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import java.util.concurrent.CompletableFuture

class BoundDevice(
        private val mContext: Context,
        private val mDeviceAddress: DeviceAddress? = null
) : DeviceWrapper {

    companion object {
        const val TAG = "BinderDevice"
    }

    val onBind: CompletableFuture<IDevice> = CompletableFuture()

    private var mDeviceBinder: DeviceService.Binder? = null
    private val mDeviceServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d(TAG, "onServiceConnected(): ")
            mDeviceBinder = service as DeviceService.Binder
            onBind.complete(this@BoundDevice)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d(TAG, "onServiceDisconnected():")
            mDeviceBinder = null
        }
    }

    val isBound: Boolean = mDeviceBinder != null

    fun bind() {
        Intent(mContext, DeviceService::class.java).also { intent ->
            mDeviceAddress?.let {
                intent.putExtra(DeviceService.EXTRA_DEVICE, it)
            }
            mContext.bindService(intent, mDeviceServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override val wrappedDevice: IDevice
        get() {
            return mDeviceBinder!!
        }


}