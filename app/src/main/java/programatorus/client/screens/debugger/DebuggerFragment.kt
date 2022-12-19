package programatorus.client.screens.debugger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import programatorus.client.screens.flashrequest.FlashRequestFragment
import programatorus.client.screens.home.HomeFragmentArgs
import java.util.concurrent.CompletableFuture


class DebuggerFragment : Fragment() {

    private var _binding: FragmentDebuggerBinding? = null

    private val device = RemoteContext.device
    private val args: DebuggerFragmentArgs by navArgs()
    private lateinit var future: CompletableFuture<String>

    private val binding get() = _binding!!

    private val resultsHandler: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val message = intent.getStringExtra(LINE_KEY)
            receive(message)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDebuggerBinding.inflate(inflater, container, false)

        device.startDebugger(
            Board(args.board, true),
            Firmware(args.firmware, true)
        ).get()

        requireContext().registerReceiver(resultsHandler, IntentFilter(DEBUGGER_LINE))
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
            val intent = Intent(DEBUGGER_LINE)
            intent.putExtra(LINE_KEY, line)
            requireContext().sendBroadcast(intent)
            Log.d(TAG, "line polled: $line")
            loop()
        }
    }

    private fun receive(line: String?) {
        line?.let {
            binding.messageList.addMessage(MessageListItem.remote(it.trim()))
            binding.messageList.scrollToBottom()
        }
    }

    fun sendCommand() {
        val command = binding.commandToSend.text.toString() + "\n"
        binding.messageList.addMessage(MessageListItem.local(command.trim()))
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
        val DEBUGGER_LINE = "programatorus.client.DEBUGGER_LINE"
        val LINE_KEY = "line"
    }
}