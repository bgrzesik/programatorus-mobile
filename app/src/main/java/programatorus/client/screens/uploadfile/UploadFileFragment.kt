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
import androidx.fragment.app.Fragment
import programatorus.client.databinding.FragmentUploadFileBinding
import java.io.FileInputStream


class UploadFileFragment : Fragment() {

    private var _binding: FragmentUploadFileBinding? = null

    private val binding get() = _binding!!

    private lateinit var contentResolver: ContentResolver

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
            selectFile()
        }

        contentResolver = requireContext().contentResolver

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun selectFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, PICK_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == PICK_FILE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { documentUri ->
                contentResolver.takePersistableUriPermission(
                    documentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                openFile(documentUri)
            }
        }
    }

    private fun openFile(documentUri: Uri) {
        val fileDescriptor = contentResolver.openFileDescriptor(documentUri, "r") ?: return
        val fileInputStream = FileInputStream(fileDescriptor.fileDescriptor)
        uploadFile(fileInputStream)
        Log.d("OPEN_FILE", "openFile: ${fileInputStream.available()}")
    }

    private fun uploadFile(stream: FileInputStream) {
        // TODO: MSG upload file
    }

    companion object {
        const val PICK_FILE = 2
    }
}