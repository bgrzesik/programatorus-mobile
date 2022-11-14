package programatorus.client.screens.boards.favorites

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import programatorus.client.databinding.ComponentFavBoardsListBinding

class FavBoardsListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr){

    private val binding: ComponentFavBoardsListBinding

    private val boardsAdapter: BoardListAdapter

    init {
        binding = ComponentFavBoardsListBinding.inflate(
            LayoutInflater.from(context),
            this
        )
        boardsAdapter = BoardListAdapter()

        binding.componentBoardsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = boardsAdapter
        }
    }

    fun enableTouch(){
        itemTouchHelper.attachToRecyclerView(binding.componentBoardsList)
    }

    fun setClickListener(clickListener: ClickListener){
        boardsAdapter.setClickListener(clickListener)
    }

    fun setBoards(boards: List<FavBoardsListItem>) {
        boardsAdapter.setItems(boards)
    }

    fun getBoards(): List<FavBoardsListItem> = boardsAdapter.getItems()

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END, 0) {

            override fun onMove(recyclerView: RecyclerView,
                                viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean {
                val adapter = recyclerView.adapter as BoardListAdapter
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                adapter.moveItem(from, to)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.5f
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)

                viewHolder.itemView.alpha = 1.0f
            }
        }

        ItemTouchHelper(simpleItemTouchCallback)
    }
}

