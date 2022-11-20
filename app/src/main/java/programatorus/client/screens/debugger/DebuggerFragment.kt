package programatorus.client.screens.debugegr

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        binding.btnSend.setOnClickListener {
            sendCommand()
        }
    }
    fun sendCommand() {
        // TODO: MSG
        val command = binding.commandToSend.text.toString()
        Log.d("DEBUGGER", "sendCommand: command")
        binding.messageList.addMessage(MessageListItem.local(command))
        binding.messageList.scrollToBottom()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}