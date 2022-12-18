package programatorus.client.screens.uploadfile

import android.content.Context
import androidx.appcompat.app.AlertDialog
import programatorus.client.R

object DeleteAlert {

    fun confirm(
        context: Context,
        message: String,
        onConfirm: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(R.string.delete_file_title)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton("delete") { _, _ -> onConfirm() }
            .setNegativeButton("dismiss") { _, _ -> null }
            .show()

    }

}