package programatorus.client.screens.choosedevice.devicelist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import programatorus.client.databinding.DeviceListItemBinding

typealias ClickListener = (target: DeviceListItem) -> Unit

class DeviceListAdapter():
    RecyclerView.Adapter<DeviceListAdapter.DeviceItemViewHolder>() {

    var devices: MutableList<DeviceListItem> = mutableListOf()

    private var onItemClickListener: ClickListener = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceItemViewHolder {
        val binding = DeviceListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return DeviceItemViewHolder(binding)
    }

    fun setClickListener(clickListener: ClickListener) {
        onItemClickListener = clickListener
    }

    override fun onBindViewHolder(holder: DeviceItemViewHolder, position: Int) {
        holder.bind(devices[position], onItemClickListener)
    }

    override fun getItemCount() = devices.size

    fun setItems(devices: MutableList<DeviceListItem>) {
        this.devices = devices
        notifyDataSetChanged()
    }

    fun addItem(device: DeviceListItem) {
        if (!devices.contains(device)) {
            devices.add(device)
            notifyItemInserted(devices.size - 1)
        }
    }

    inner class DeviceItemViewHolder(private val binding: DeviceListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DeviceListItem, onItemClickListener: ClickListener) {
            binding.deviceAddress.text = item.address
            binding.deviceName.text = item.name

            binding.root.setOnClickListener {
                onItemClickListener(item)
            }
        }

    }
}