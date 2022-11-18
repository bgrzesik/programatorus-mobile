package programatorus.client.screens.debugger.messagelist

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import programatorus.client.R
import programatorus.client.databinding.MessageListItemBinding
import programatorus.client.screens.debugger.messagelist.MessageListAdapter.Colors.MY_COLOR
import programatorus.client.screens.debugger.messagelist.MessageListAdapter.Colors.REMOTE_COLOR

typealias ClickListener = (target: MessageListItem) -> Unit

class MessageListAdapter():
    RecyclerView.Adapter<MessageListAdapter.MessageItemViewHolder>() {

    var messages: MutableList<MessageListItem> = mutableListOf()

    private var onItemClickListener: ClickListener = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageItemViewHolder {
        val binding = MessageListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return MessageItemViewHolder(binding)
    }

    fun setClickListener(clickListener: ClickListener) {
        onItemClickListener = clickListener
    }

    override fun onBindViewHolder(holder: MessageItemViewHolder, position: Int) {
        holder.bind(messages[position], onItemClickListener)
    }

    override fun getItemCount() = messages.size

    fun setItems(messages: MutableList<MessageListItem>) {
        this.messages = messages
        notifyDataSetChanged()
    }

    fun addItem(item: MessageListItem) {
        messages.add(item)
        notifyItemInserted(messages.size - 1)
    }

    inner class MessageItemViewHolder(private val binding: MessageListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MessageListItem, onItemClickListener: ClickListener) {
            binding.message.text = item.message

            binding.message.setTextColor(
                if (item.isMy) MY_COLOR else REMOTE_COLOR
            )

            binding.root.setOnClickListener {
                onItemClickListener(item)
            }
        }

    }
    private object Colors {
        val MY_COLOR = Color.argb(255, 250,250,150)
        val REMOTE_COLOR = Color.argb(255, 225,225,225)
    }
}

