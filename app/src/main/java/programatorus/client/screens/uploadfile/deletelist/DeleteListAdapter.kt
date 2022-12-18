package programatorus.client.screens.uploadfile.deletelist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import programatorus.client.databinding.DeleteListItemBinding

typealias ClickListener = (target: DeleteListItem) -> Unit

class DeleteListAdapter():
    RecyclerView.Adapter<DeleteListAdapter.DeleteItemViewHolder>() {

    var files: MutableList<DeleteListItem> = mutableListOf()

    private lateinit var context: Context

    private var onItemClickListener: ClickListener = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeleteItemViewHolder {
        context = parent.context
        val binding = DeleteListItemBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false)
        return DeleteItemViewHolder(binding)
    }

    fun setClickListener(clickListener: ClickListener) {
        onItemClickListener = clickListener
    }

    override fun onBindViewHolder(holder: DeleteItemViewHolder, position: Int) {
        holder.bind(files[position], onItemClickListener)
    }

    override fun getItemCount() = files.size

    fun setItems(items: MutableList<DeleteListItem>) {
        this.files = items
        notifyDataSetChanged()
    }

    fun deleteItem(item: DeleteListItem) {
        val index = files.indexOf(item)
        if (index >= 0) {
            files.removeAt(index)
        }
        notifyItemRemoved(index)
    }

    inner class DeleteItemViewHolder(private val binding: DeleteListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DeleteListItem, onItemClickListener: ClickListener) {
            with(binding) {
                filename.text = item.name

                btn.setOnClickListener {
                    onItemClickListener(item)
                }
            }
        }
    }

}

