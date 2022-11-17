package programatorus.client.screens.firmware.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import programatorus.client.databinding.FavFirmwaresListItemBinding

typealias ClickListener = (target: FavFirmwaresListItem) -> Unit

class FirmwareListAdapter():
    RecyclerView.Adapter<FirmwareListAdapter.FirmwareItemViewHolder>() {

    val differ = AsyncListDiffer(
        this,
        object : DiffUtil.ItemCallback<FavFirmwaresListItem>() {
            override fun areItemsTheSame(item: FavFirmwaresListItem, other: FavFirmwaresListItem): Boolean =
                item == other

            override fun areContentsTheSame(item: FavFirmwaresListItem, other: FavFirmwaresListItem): Boolean =
                item == other
        })

    private var onItemClickListener: ClickListener = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FirmwareItemViewHolder {
        val binding = FavFirmwaresListItemBinding.inflate(
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

    fun setItems(items: List<FavFirmwaresListItem>) {
        differ.submitList(items)
        notifyDataSetChanged()
    }

    fun getItems(): MutableList<FavFirmwaresListItem> {
        return differ.currentList
    }

    fun moveItem(from: Int, to: Int) {
        val list = differ.currentList.toMutableList()
        val item = list[from]
        list.removeAt(from)
        list.add(to, item)
        differ.submitList(list)
    }

    inner class FirmwareItemViewHolder(private val binding: FavFirmwaresListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FavFirmwaresListItem, onItemClickListener: ClickListener) {
            with(binding) {
                filename.text = item.name

                root.setOnClickListener {
                    onItemClickListener(item)
                }
            }
        }
    }
}
