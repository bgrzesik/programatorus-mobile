package programatorus.client.screens.firmware.all

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
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

}

