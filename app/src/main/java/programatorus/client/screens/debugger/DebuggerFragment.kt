package programatorus.client.screens.debugger

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import programatorus.client.RemoteContext
import programatorus.client.databinding.FragmentDebuggerBinding
import programatorus.client.model.Board
import programatorus.client.model.Firmware
import programatorus.client.screens.debugger.messagelist.MessageListItem
import programatorus.client.screens.home.HomeFragmentArgs
import java.util.concurrent.CompletableFuture


class DebuggerFragment : Fragment() {

    private var _binding: FragmentDebuggerBinding? = null

    private val device = RemoteContext.device
    private val args: DebuggerFragmentArgs by navArgs()
    private lateinit var future: CompletableFuture<String>

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDebuggerBinding.inflate(inflater, container, false)

        device.startDebugger(
            Board(args.board, true),
            Firmware(args.firmware, true)
        ).get()

        startLoop()

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSend.setOnClickListener {
            sendCommand()
        }
    }

    private fun startLoop() {
        future = CompletableFuture()
        loop()
    }

    private fun loop() {
        device.pollDebuggerLine().thenAccept { line ->
            Log.d(TAG, "line polled: $line")
            receive(line)
            loop()
        }
    }

    private fun receive(line: String) {
        binding.messageList.addMessage(MessageListItem.remote(line))
        binding.messageList.scrollToBottom()
    }

    fun sendCommand() {
        val command = binding.commandToSend.text.toString()
        binding.messageList.addMessage(MessageListItem.local(command))
        binding.messageList.scrollToBottom()
        device.sendDebuggerLine(command)
    }

    override fun onDestroyView() {
        device.stopDebugger().get()
        future.cancel(true)
        super.onDestroyView()
        _binding = null
    }


    companion object {
        val TAG = "DEBUGGER"
    }
}