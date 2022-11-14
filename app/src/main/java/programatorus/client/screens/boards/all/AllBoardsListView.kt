package programatorus.client.screens.boards.all

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
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

}

