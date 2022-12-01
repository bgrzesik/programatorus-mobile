package programatorus.client.screens.uploadfile

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import programatorus.client.databinding.FragmentUploadFileBinding
import programatorus.client.device.BoundDevice
import programatorus.client.device.IDevice
import java.io.FileInputStream


class UploadFileFragment : Fragment() {

    private var _binding: FragmentUploadFileBinding? = null

    private val binding get() = _binding!!

    private lateinit var contentResolver: ContentResolver
    private lateinit var _device: BoundDevice

    private var _selectFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
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

        binding.btnChooseFile.setOnClickListener {
            _selectFile.launch("*/*")
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
        val fileInputStream = FileInputStream(fileDescriptor.fileDescriptor)
        uploadFile(fileInputStream)
        Log.d("OPEN_FILE", "openFile: ${fileInputStream.available()}")

        _device.onBind.thenAccept { device ->
            device.upload(documentUri)
        }
    }

    private fun uploadFile(stream: FileInputStream) {
        // TODO: MSG upload file
    }

    companion object {
        const val PICK_FILE = 2
    }
}