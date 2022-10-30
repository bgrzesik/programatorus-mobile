package programatorus.client.screens.boards

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import programatorus.client.databinding.BoardListItemBinding

typealias ClickListener = (target: BoardListItem) -> Unit

class BoardListAdapter():
    RecyclerView.Adapter<BoardListAdapter.BoardItemViewHolder>() {

    val differ = AsyncListDiffer(
        this,
        object : DiffUtil.ItemCallback<BoardListItem>() {
            override fun areItemsTheSame(item: BoardListItem, other: BoardListItem): Boolean =
                item == other

            override fun areContentsTheSame(item: BoardListItem, other: BoardListItem): Boolean =
                item == other
        })

    private var onItemClickListener: ClickListener = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardItemViewHolder {
        val binding = BoardListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return BoardItemViewHolder(binding)
    }

    fun setClickListener(clickListener: ClickListener) {
        onItemClickListener = clickListener
    }

    override fun onBindViewHolder(holder: BoardItemViewHolder, position: Int) {
        val item= differ.currentList[position]
        holder.bind(item, onItemClickListener)
    }


    override fun getItemCount() = differ.currentList.size

    fun setItems(boards: List<BoardListItem>) {
        differ.submitList(boards)
        notifyDataSetChanged()
    }

    fun getItems(): MutableList<BoardListItem> {
//        notifyDataSetChanged()
        return differ.currentList
    }

    fun moveItem(from: Int, to: Int) {
        val list = differ.currentList.toMutableList()
        val item = list[from]
        list.removeAt(from)
        val target = if (to < from) to + 1 else to - 1
        list.add(target, item)
        differ.submitList(list)
    }

    inner class BoardItemViewHolder(private val binding: BoardListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BoardListItem, onItemClickListener: ClickListener) {
            binding.filename.text = item.name

            binding.root.setOnClickListener {
                onItemClickListener(item)
            }
        }
    }
}
