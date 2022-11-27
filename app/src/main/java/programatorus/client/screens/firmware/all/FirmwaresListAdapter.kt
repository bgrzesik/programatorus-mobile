package programatorus.client.screens.firmware.all

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import programatorus.client.databinding.AllFirmwaresListItemBinding

typealias ClickListener = (target: AllFirmwaresListItem) -> Unit

class FirmwaresListAdapter():
    RecyclerView.Adapter<FirmwaresListAdapter.FirmwareItemViewHolder>() {

    val differ = AsyncListDiffer(
        this,
        object : DiffUtil.ItemCallback<AllFirmwaresListItem>() {
            override fun areItemsTheSame(item: AllFirmwaresListItem, other: AllFirmwaresListItem): Boolean =
                item == other

            override fun areContentsTheSame(item: AllFirmwaresListItem, other: AllFirmwaresListItem): Boolean =
                item == other
        })

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
        val item= differ.currentList[position]
        holder.bind(item, onItemClickListener)
    }


    override fun getItemCount() = differ.currentList.size

    fun setItems(items: List<AllFirmwaresListItem>) {
        differ.submitList(items)
        notifyDataSetChanged()
    }

    fun getItems(): List<AllFirmwaresListItem> {
        return differ.currentList
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
