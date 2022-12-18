package programatorus.client.screens.uploadfile

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import programatorus.client.R
import programatorus.client.RemoteContext
import programatorus.client.RemoteContext.device
import programatorus.client.databinding.FragmentUploadFileBinding
import programatorus.client.device.BoundDevice
import programatorus.client.screens.uploadfile.deletelist.DeleteListItem
import programatorus.client.shared.LoadingDialog


class UploadFileFragment : Fragment() {

    private var _binding: FragmentUploadFileBinding? = null

    private val binding get() = _binding!!

    private lateinit var contentResolver: ContentResolver
    private lateinit var _device: BoundDevice

    private var _selectFile =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                openFile(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentUploadFileBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            btnChooseFile.setOnClickListener {
                _selectFile.launch(arrayOf("*/*"))
            }

            deletableList.setItems(
                RemoteContext.firmwareService.getAll().map {
                    DeleteListItem(it.name)
                })

            deletableList.setClickListener {
                DeleteAlert.confirm(
                    requireContext(),
                    getString(R.string.delete_file_text, it.name)
                ) {
                    val dialog = LoadingDialog.loadingDialog(layoutInflater, requireContext())
                    device.deleteFirmware(it.name).thenAccept {
                        dialog.dismiss()
                    }
                    deletableList.removeItem(it)
                }
            }
        }



        contentResolver = requireContext().contentResolver
        _device = BoundDevice(requireContext())
        _device.bind()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openFile(documentUri: Uri) {
        val fileDescriptor = contentResolver.openFileDescriptor(documentUri, "r") ?: return
        val dialog = LoadingDialog.loadingDialog(layoutInflater, requireContext())
        device.upload(documentUri).thenAccept {
            dialog.dismiss()
        }
    }

}