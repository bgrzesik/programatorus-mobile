package programatorus.client.screens.boards.all

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.recyclerview.widget.*
import programatorus.client.databinding.AllBoardsListItemBinding

typealias ClickListener = (target: AllBoardsListItem) -> Unit

class BoardListAdapter():
    RecyclerView.Adapter<BoardListAdapter.BoardItemViewHolder>() {

    val differ = AsyncListDiffer(
        this,
        object : DiffUtil.ItemCallback<AllBoardsListItem>() {
            override fun areItemsTheSame(item: AllBoardsListItem, other: AllBoardsListItem): Boolean =
                item == other

            override fun areContentsTheSame(item: AllBoardsListItem, other: AllBoardsListItem): Boolean =
                item == other
        })

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
        val item= differ.currentList[position]
        holder.bind(item, onItemClickListener)
    }


    override fun getItemCount() = differ.currentList.size

    fun setItems(boards: List<AllBoardsListItem>) {
        differ.submitList(boards)
        notifyDataSetChanged()
    }

    fun getItems(): MutableList<AllBoardsListItem> {
        return differ.currentList
    }

//    fun toggleFavorite(item: AllBoardsListItem) {
//        differ.submitList(
//            differ.currentList.map {
//                if (it == item) item.toggle() else it
//            }
//        )
//    }

    inner class BoardItemViewHolder(private val binding: AllBoardsListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AllBoardsListItem, onItemClickListener: ClickListener) {
            binding.filename.text = item.name
            binding.checkbox.isChecked = item.isSelected.get()

            binding.checkbox.setOnClickListener {
                item.toggle()
            }

            binding.root.setOnClickListener {
                onItemClickListener(item)
            }
        }
    }
}
