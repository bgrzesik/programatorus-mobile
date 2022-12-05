package programatorus.client.shared

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import programatorus.client.R

object LoadingDialog {

    fun loadingDialog(layoutInflater: LayoutInflater, context: Context): AlertDialog {
        val view: View = layoutInflater.inflate(R.layout.loading_layout, null)

        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            .create()
        dialog.show()
        return dialog
    }

}