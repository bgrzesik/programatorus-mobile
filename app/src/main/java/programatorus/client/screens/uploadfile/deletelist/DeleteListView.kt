package programatorus.client.screens.uploadfile.deletelist

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import programatorus.client.databinding.ComponentDeleteListBinding

class DeleteListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr){

    private val binding: ComponentDeleteListBinding

    private val deletableAdapter: DeleteListAdapter

    init {
        binding = ComponentDeleteListBinding.inflate(
            LayoutInflater.from(context),
            this
        )
        deletableAdapter = DeleteListAdapter()

        binding.componentDeleteList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = deletableAdapter
        }
    }

    fun setClickListener(clickListener: ClickListener){
        deletableAdapter.setClickListener(clickListener)
    }

    fun setItems(items: List<DeleteListItem>) {
        deletableAdapter.setItems(items.toMutableList())
    }

    fun removeItem(item: DeleteListItem) {
        deletableAdapter.deleteItem(item)
    }

}

