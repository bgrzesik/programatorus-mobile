package programatorus.client.screens.boards.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import programatorus.client.databinding.FavBoardsListItemBinding

typealias ClickListener = (target: FavBoardsListItem) -> Unit

class BoardListAdapter():
    RecyclerView.Adapter<BoardListAdapter.BoardItemViewHolder>() {

    val differ = AsyncListDiffer(
        this,
        object : DiffUtil.ItemCallback<FavBoardsListItem>() {
            override fun areItemsTheSame(item: FavBoardsListItem, other: FavBoardsListItem): Boolean =
                item == other

            override fun areContentsTheSame(item: FavBoardsListItem, other: FavBoardsListItem): Boolean =
                item == other
        })

    private var onItemClickListener: ClickListener = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardItemViewHolder {
        val binding = FavBoardsListItemBinding.inflate(
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

    fun setItems(boards: List<FavBoardsListItem>) {
        differ.submitList(boards)
        notifyDataSetChanged()
    }

    fun getItems(): MutableList<FavBoardsListItem> {
        return differ.currentList
    }

    fun moveItem(from: Int, to: Int) {
        val list = differ.currentList.toMutableList()
        val item = list[from]
        list.removeAt(from)
        list.add(to, item)
        differ.submitList(list)
    }

    inner class BoardItemViewHolder(private val binding: FavBoardsListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FavBoardsListItem, onItemClickListener: ClickListener) {
            binding.filename.text = item.name

            binding.root.setOnClickListener {
                onItemClickListener(item)
            }
        }
    }
}
