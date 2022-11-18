package programatorus.client.screens.debugger.messagelist

data class MessageListItem(
    val message: String,
    val isMy: Boolean
) {
    companion object {
        fun my(message: String) = MessageListItem(message, true)
        fun remote(message: String) = MessageListItem(message, false)
    }
}
