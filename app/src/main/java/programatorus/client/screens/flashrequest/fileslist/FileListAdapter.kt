package programatorus.client.screens.flashrequest.fileslist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import programatorus.client.R
import programatorus.client.databinding.FilesListItemBinding

typealias ClickListener = (target: FileListItem) -> Unit

class FileListAdapter():
    RecyclerView.Adapter<FileListAdapter.ExpandableFilesItemViewHolder>() {

    var files: List<FileListItem> = listOf()

    var all: List<FileListItem> = listOf()

    private lateinit var context: Context

    private var onItemClickListener: ClickListener = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpandableFilesItemViewHolder {
        context = parent.context
        val binding = FilesListItemBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false)
        return ExpandableFilesItemViewHolder(binding)
    }

    fun setClickListener(clickListener: ClickListener) {
        onItemClickListener = clickListener
    }

    override fun onBindViewHolder(holder: ExpandableFilesItemViewHolder, position: Int) {
        holder.bind(files[position], onItemClickListener)
    }

    override fun getItemCount() = files.size

    fun setItems(items: List<FileListItem>) {
        all = items
        this.files = items
        notifyDataSetChanged()
    }

    fun filterWith(query: (item: FileListItem) -> Boolean) {
        this.files = all.filter(query)
        notifyDataSetChanged()
    }

    inner class ExpandableFilesItemViewHolder(private val binding: FilesListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FileListItem, onItemClickListener: ClickListener) {
            binding.filename.text = item.name

            binding.img.background = if (item.isFavorite) checked() else unchecked()


            binding.root.setOnClickListener {
                onItemClickListener(item)
            }
        }
    }


    private fun checked() = context.getDrawable(R.drawable.ic_favorite_checked)
    private fun unchecked() = context.getDrawable(R.drawable.ic_favorite_unchecked)
}

