package programatorus.client.screens.flashrequest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import programatorus.client.R
import programatorus.client.RemoteContext
import programatorus.client.RemoteContext.flashService
import programatorus.client.databinding.FragmentFlashRequestBinding
import programatorus.client.model.Board
import programatorus.client.model.Firmware
import programatorus.client.screens.flashrequest.fileslist.Alert
import programatorus.client.screens.flashrequest.fileslist.FileListItem


class FlashRequestFragment : Fragment() {

    private var _binding: FragmentFlashRequestBinding? = null

    private val binding get() = _binding!!

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

        binding.btnChooseBoard.setOnClickListener {
            chooseBoardAlert()
        }

        binding.btnChooseFirmware.setOnClickListener {
            chooseFirmwareAlert()
        }

        binding.btnSubmit.setOnClickListener {
            sendRequest()
        }
    }

    private fun chooseBoardAlert() {
        val title = getString(R.string.selected_board_alert)
        chooseFileAlert(title,
            selectedBoard,
            RemoteContext.boardsService.getAll().map {
                FileListItem(it.name, it.isFavorite)
            }) {
            selectedBoard = it
            updateBoardText()
        }
    }

    private fun chooseFirmwareAlert() {
        val title = getString(R.string.selected_firmware_alert)
        chooseFileAlert(title,
            selectedFirmware,
            RemoteContext.firmwareService.getAll().map {
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
        flashService.sendRequest(Board(selectedBoard, true), Firmware(selectedFirmware, true))
            .thenAccept {
                navigateToResults(it)
            }
    }

    fun navigateToResults(message: String) {
        val action = FlashRequestFragmentDirections.actionFlashRequestToFlashResult(message)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}