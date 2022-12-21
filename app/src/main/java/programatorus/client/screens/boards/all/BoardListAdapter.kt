package programatorus.client.screens.boards.all

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import programatorus.client.databinding.AllBoardsListItemBinding

typealias ClickListener = (target: AllBoardsListItem) -> Unit

class BoardListAdapter():
    RecyclerView.Adapter<BoardListAdapter.BoardItemViewHolder>() {

    var filtered: List<AllBoardsListItem> = listOf()

    var all: List<AllBoardsListItem> = listOf()

    private var onItemClickListener: ClickListener = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardItemViewHolder {
        val binding = AllBoardsListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return BoardItemViewHolder(binding)
    }

    fun setClickListener(clickListener: ClickListener) {
        onItemClickListener = clickListener
    }

    override fun onBindViewHolder(holder: BoardItemViewHolder, position: Int) {
        holder.bind(filtered[position], onItemClickListener)
    }

    override fun getItemCount() = filtered.size

    fun setItems(items: List<AllBoardsListItem>) {
        all = items
        filtered = items
        notifyDataSetChanged()
    }

    fun getItems(): List<AllBoardsListItem> {
        return all
    }

    fun filterWith(query: (item: AllBoardsListItem) -> Boolean) {
        filtered = all.filter(query)
        notifyDataSetChanged()
    }

    inner class BoardItemViewHolder(private val binding: AllBoardsListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AllBoardsListItem, onItemClickListener: ClickListener) {
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
