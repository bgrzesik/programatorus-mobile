package programatorus.client.screens.firmware.all

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import programatorus.client.databinding.AllFirmwaresListItemBinding
import programatorus.client.screens.boards.all.AllBoardsListItem

typealias ClickListener = (target: AllFirmwaresListItem) -> Unit

class FirmwaresListAdapter():
    RecyclerView.Adapter<FirmwaresListAdapter.FirmwareItemViewHolder>() {

    var filtered: List<AllFirmwaresListItem> = listOf()

    var all: List<AllFirmwaresListItem> = listOf()

    private var onItemClickListener: ClickListener = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FirmwareItemViewHolder {
        val binding = AllFirmwaresListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return FirmwareItemViewHolder(binding)
    }

    fun setClickListener(clickListener: ClickListener) {
        onItemClickListener = clickListener
    }

    override fun onBindViewHolder(holder: FirmwareItemViewHolder, position: Int) {
        holder.bind(filtered[position], onItemClickListener)
    }


    override fun getItemCount() = filtered.size

    fun setItems(items: List<AllFirmwaresListItem>) {
        all = items
        filtered = items
        notifyDataSetChanged()
    }

    fun getItems(): List<AllFirmwaresListItem> {
        return all
    }

    fun filterWith(query: (item: AllFirmwaresListItem) -> Boolean) {
        filtered = all.filter(query)
        notifyDataSetChanged()
    }

    inner class FirmwareItemViewHolder(private val binding: AllFirmwaresListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AllFirmwaresListItem, onItemClickListener: ClickListener) {
            with(binding) {
                filename.text = item.name
                checkbox.isChecked = item.isSelected.get()

                checkbox.setOnClickListener {
                    item.toggle()
                }

                root.setOnClickListener {
                    onItemClickListener(item)
                }
            }
        }
    }
}
