package programatorus.client.screens.flashrequest.fileslist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import programatorus.client.R
import programatorus.client.databinding.ChooseFileFromListBinding

object Alert {

    fun chooseFile(
        context: Context,
        layoutInflater: LayoutInflater,
        list: List<FileListItem>,
        title: String,
        currentFile: String,
        onConfirm: (text: String) -> Unit
    ) {

        val view: View = layoutInflater.inflate(R.layout.choose_file_from_list, null)
        val binding = ChooseFileFromListBinding.bind(view)

        val filesAdapter = FileListAdapter()
        val linearLayoutManager = LinearLayoutManager(context)
        binding.componentMessageList.apply {
            layoutManager = linearLayoutManager
            adapter = filesAdapter
        }

        binding.chooseFileText.text = currentFile
        binding.chooseFileTitle.text = title

        val clickListener: ClickListener = {
            binding.chooseFileText.text = it.name
        }

        filesAdapter.setClickListener(clickListener)


        filesAdapter.setItems(list as MutableList<FileListItem>)

        setupSearch(binding, filesAdapter)

        AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(true)
            .setPositiveButton("confirm") { _, _ -> onConfirm(getFilename(binding)) }
            .setNegativeButton("dismiss") { _, _ -> null }
            .show()

    }

    fun getFilename(binding: ChooseFileFromListBinding): String =
        binding.chooseFileText.text as String

    private fun setupSearch(binding: ChooseFileFromListBinding, filesAdapter: FileListAdapter) {
        binding.search.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }

                override fun onQueryTextChange(text: String?): Boolean {
                    filesAdapter.filterWith { Search.matchesText(it, text ?: "") }
                    return false
                }
            }
        )
    }

    object Search {

        fun matchesText(fileListItem: FileListItem, query: String): Boolean =
            fileListItem.name.lowercase().contains(query.lowercase())

    }
}