package programatorus.client.screens.boards.all

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import programatorus.client.databinding.ComponentAllBoardsListBinding

class AllBoardsListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr){

    private val binding: ComponentAllBoardsListBinding

    private val boardsAdapter: BoardListAdapter

    init {
        binding = ComponentAllBoardsListBinding.inflate(
            LayoutInflater.from(context),
            this
        )
        boardsAdapter = BoardListAdapter()

        binding.search.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }

                override fun onQueryTextChange(text: String?): Boolean {
                    boardsAdapter.filterWith { Search.matchesText(it, text ?: "") }
                    return false
                }
            }
        )

        binding.componentBoardsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = boardsAdapter
        }
    }

    fun setClickListener(clickListener: ClickListener){
        boardsAdapter.setClickListener(clickListener)
    }

    fun setBoards(boards: List<AllBoardsListItem>) {
        boardsAdapter.setItems(boards)
    }

    fun getBoards(): List<AllBoardsListItem> = boardsAdapter.getItems()

    private object Search {

        fun matchesText(item: AllBoardsListItem, query: String): Boolean =
            item.name.lowercase().contains(query.lowercase())

    }
}

