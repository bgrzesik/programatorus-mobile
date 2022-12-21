package programatorus.client.screens.firmware.all

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import programatorus.client.databinding.ComponentAllFirmwaresListBinding

class AllFirmwaresListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr){

    private val binding: ComponentAllFirmwaresListBinding

    private val FirmwaresAdapter: FirmwaresListAdapter

    init {
        binding = ComponentAllFirmwaresListBinding.inflate(
            LayoutInflater.from(context),
            this
        )
        FirmwaresAdapter = FirmwaresListAdapter()

        binding.search.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }

                override fun onQueryTextChange(text: String?): Boolean {
                    FirmwaresAdapter.filterWith { Search.matchesText(it, text ?: "") }
                    return false
                }
            }
        )

        binding.componentFirmwaresList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = FirmwaresAdapter
        }
    }

    fun setClickListener(clickListener: ClickListener){
        FirmwaresAdapter.setClickListener(clickListener)
    }

    fun setFirmwares(items: List<AllFirmwaresListItem>) {
        FirmwaresAdapter.setItems(items)
    }

    fun getFirmwares(): List<AllFirmwaresListItem> = FirmwaresAdapter.getItems()

    private object Search {

        fun matchesText(item: AllFirmwaresListItem, query: String): Boolean =
            item.name.lowercase().contains(query.lowercase())

    }
}

