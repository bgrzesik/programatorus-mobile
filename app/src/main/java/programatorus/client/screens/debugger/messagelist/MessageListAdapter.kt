package programatorus.client.screens.debugger.messagelist

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import programatorus.client.R
import programatorus.client.databinding.MessageListItemBinding

typealias ClickListener = (target: MessageListItem) -> Unit

class MessageListAdapter():
    RecyclerView.Adapter<MessageListAdapter.MessageItemViewHolder>() {

    var messages: MutableList<MessageListItem> = mutableListOf()

    private lateinit var context: Context

    private var onItemClickListener: ClickListener = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageItemViewHolder {
        context = parent.context
        val binding = MessageListItemBinding.inflate(
            LayoutInflater.from(context),
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
        Log.d("DEBUGGER", "items: $messages")
        notifyItemInserted(messages.size - 1)
    }

    inner class MessageItemViewHolder(private val binding: MessageListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MessageListItem, onItemClickListener: ClickListener) {
            binding.message.text = item.message

            binding.message.setTextColor(
                if (item.isLocal) localColor() else remoteColor()
            )

            binding.root.setOnClickListener {
                onItemClickListener(item)
            }
        }

    }
    private fun localColor() = context.getColor(R.color.debuggerLocal)
    private fun remoteColor() = context.getColor(R.color.debuggerRemote)
}

