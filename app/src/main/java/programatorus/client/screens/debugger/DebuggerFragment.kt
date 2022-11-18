package programatorus.client.screens.debugegr

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.Fragment
import programatorus.client.databinding.FragmentDebuggerBinding
import programatorus.client.screens.debugger.messagelist.MessageListItem


class DebuggerFragment : Fragment() {

    private var _binding: FragmentDebuggerBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDebuggerBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
//            messageList.setMessages(
//                listOf(
//                    MessageListItem.my("XD"),
//                    MessageListItem.remote("ddddd"),
//                    MessageListItem.my("mmmddd    ekanfja   jkhfkjahjkfahjdfk  xxxxxxxxxxxx")
//                )
//            )

            btnSend.setOnClickListener {
                sendCommand()
            }

//            commandToSend.setOnEditorActionListener(writeListener)
        }
    }

    private val writeListener =
        OnEditorActionListener { view, actionId, event ->
            if (actionId == EditorInfo.IME_NULL && event.action === KeyEvent.ACTION_UP) {
                sendCommand()
            }
            true
        }

    fun sendCommand() {
        // TODO: MSG
        val command = binding.commandToSend.text.toString()
        Log.d("DEBUGGER", "sendCommand: command")
        binding.messageList.addMessage(MessageListItem.my(command))
        binding.messageList.scrollToBottom()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}