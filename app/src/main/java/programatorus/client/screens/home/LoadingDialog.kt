package programatorus.client.screens.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import programatorus.client.R
import programatorus.client.databinding.LoadingLayoutBinding

object LoadingDialog {

    fun loadingDialog(layoutInflater: LayoutInflater, context: Context): AlertDialog {
        val view: View = layoutInflater.inflate(R.layout.loading_layout, null)
//        val binding = LoadingLayoutBinding.bind(view)

        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            .create()
        dialog.show()
        return dialog
    }

}