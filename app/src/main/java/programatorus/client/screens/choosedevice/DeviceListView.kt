package programatorus.client.screens.choosedevice

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import programatorus.client.databinding.ComponentChooseDeviceListBinding

class DeviceListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr){

    private val binding: ComponentChooseDeviceListBinding

    private val devicesAdapter: DeviceListAdapter

    init {
        binding = ComponentChooseDeviceListBinding.inflate(
            LayoutInflater.from(context),
            this
        )
        devicesAdapter = DeviceListAdapter()
        binding.componentDeviceList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = devicesAdapter
        }
    }

    fun setClickListener(clickListener: ClickListener){
        devicesAdapter.setClickListener(clickListener)
    }

    fun setDevices(devices: List<DeviceListItem>) {
        devicesAdapter.setItems(devices as MutableList<DeviceListItem>)
    }

    fun addDevice(device: DeviceListItem) {
        devicesAdapter.addItem(device)
    }

}

