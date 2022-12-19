package programatorus.client.screens.flashrequest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import programatorus.client.R
import programatorus.client.RemoteContext
import programatorus.client.databinding.FragmentFlashRequestBinding
import programatorus.client.model.Board
import programatorus.client.model.Firmware
import programatorus.client.screens.flashrequest.fileslist.Alert
import programatorus.client.screens.flashrequest.fileslist.FileListItem
import programatorus.client.shared.LoadingDialog


class FlashRequestFragment : Fragment() {

    private var _binding: FragmentFlashRequestBinding? = null

    private lateinit var dialog: AlertDialog
    private val binding get() = _binding!!
    private val device = RemoteContext.device
    private lateinit var selectedBoard: String
    private lateinit var selectedFirmware: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFlashRequestBinding.inflate(inflater, container, false)

        selectedBoard = getString(R.string.placeholder_selection)
        selectedFirmware = getString(R.string.placeholder_selection)
        updateBoardText()
        updateFirmwareText()

        return binding.root
    }

    fun showLoading() {
        dialog = LoadingDialog.loadingDialog(layoutInflater, requireContext())
    }

    fun stopLoading() {
        dialog.dismiss()
    }

    private val resultsHandler: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val message = intent.getStringExtra(MESSAGE_KEY)
            handleResults(message)
        }
    }

    private fun handleResults(message: String?) {
        stopLoading()
        message?.let {
            navigateToResults(it)
        }
    }

    private fun chooseFileAlert(
        title: String,
        currentFile: String,
        files: List<FileListItem>,
        onComplete: (text: String) -> Unit
    ) {
        Alert.chooseFile(
            requireContext(),
            layoutInflater,
            files,
            title,
            currentFile,
        ) { onComplete(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            btnChooseBoard.setOnClickListener {
                chooseBoardAlert()
            }

            btnChooseFirmware.setOnClickListener {
                chooseFirmwareAlert()
            }

            btnFlashRequest.setOnClickListener {
                if (validateSelected()) {
                    showLoading()
                    sendRequest()
                }
            }

            btnStartDebug.setOnClickListener {
                if (validateSelected())
                    navigateToDebugger()
            }
        }

        requireContext().registerReceiver(resultsHandler, IntentFilter(FLASH_RESULT))

    }

    private fun chooseBoardAlert() {
        val title = getString(R.string.selected_board_alert)
        chooseFileAlert(title,
            selectedBoard,
            RemoteContext.boardsService.getAllSorted().map {
                FileListItem(it.name, it.isFavorite)
            }) {
            selectedBoard = it
            updateBoardText()
        }
    }

    private fun validateSelected(): Boolean {
        val isValid = selectedBoard != getString(R.string.placeholder_selection) &&
            selectedFirmware != getString(R.string.placeholder_selection)
        if (!isValid)
            Toast.makeText(context, "Choose configuration files first", Toast.LENGTH_SHORT).show()
        return isValid
    }

    private fun chooseFirmwareAlert() {
        val title = getString(R.string.selected_firmware_alert)
        chooseFileAlert(title,
            selectedFirmware,
            RemoteContext.firmwareService.getAllSorted().map {
                FileListItem(it.name, it.isFavorite)
            }) {
            selectedFirmware = it
            updateFirmwareText()
        }
    }

    private fun updateFirmwareText() {
        binding.btnChooseFirmware.text = getString(R.string.selected_firmware, selectedFirmware)
    }

    private fun updateBoardText() {
        binding.btnChooseBoard.text = getString(R.string.selected_board, selectedBoard)
    }

    fun sendRequest() {
        device.flashRequest(Board(selectedBoard, true), Firmware(selectedFirmware, true))
            .thenAccept {
                val intent = Intent(FLASH_RESULT)
                intent.putExtra(MESSAGE_KEY, it)
                requireContext().sendBroadcast(intent)
            }
    }

    fun navigateToResults(message: String) {
        val action = FlashRequestFragmentDirections.actionFlashRequestToFlashResult(message)
        findNavController().navigate(action)
    }

    fun navigateToDebugger() {
        val action =
            FlashRequestFragmentDirections.actionFlashToDebugger(selectedBoard, selectedFirmware)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val FLASH_RESULT = "programatorus.client.FLASH_RESULT"
        val MESSAGE_KEY = "message"
    }

}