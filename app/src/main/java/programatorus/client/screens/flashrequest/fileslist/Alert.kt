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

        binding.chooseFileTitle.text = title

        filesAdapter.setItems(list as MutableList<FileListItem>)

        setupSearch(binding, filesAdapter)

        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(true)
            .setNegativeButton("dismiss") { _, _ -> null }
            .show()

        val clickListener: ClickListener = {
            onConfirm(it.name)
            dialog.dismiss()
        }

        filesAdapter.setClickListener(clickListener)

    }

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