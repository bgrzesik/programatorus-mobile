package programatorus.client.screens.debugger.messagelist

data class MessageListItem(
    val message: String,
    val isLocal: Boolean
) {
    companion object {
        fun local(message: String) = MessageListItem(message, true)
        fun remote(message: String) = MessageListItem(message, false)
    }
}
