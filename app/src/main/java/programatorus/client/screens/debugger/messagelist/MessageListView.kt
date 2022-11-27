package programatorus.client.screens.debugger.messagelist

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import programatorus.client.databinding.ComponentDebuggerMessageListBinding

class MessageListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr){

    private val binding: ComponentDebuggerMessageListBinding

    private val messagesAdapter: MessageListAdapter

    private lateinit var linearLayoutManager: LinearLayoutManager

    init {
        binding = ComponentDebuggerMessageListBinding.inflate(
            LayoutInflater.from(context),
            this
        )
        messagesAdapter = MessageListAdapter()

        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.stackFromEnd = true


        binding.componentMessageList.apply {
            layoutManager = linearLayoutManager
            adapter = messagesAdapter
        }
    }

    fun setClickListener(clickListener: ClickListener){
        messagesAdapter.setClickListener(clickListener)
    }

    fun scrollToBottom() {
        linearLayoutManager.scrollToPosition(messagesAdapter.itemCount - 1)
    }
    fun setMessages(messages: List<MessageListItem>) {
        messagesAdapter.setItems(messages as MutableList<MessageListItem>)
    }

    fun addMessage(message: MessageListItem) {
        messagesAdapter.addItem(message)
    }

}

