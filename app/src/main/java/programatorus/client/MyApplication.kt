package programatorus.client

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class MyApplication: Application() {



    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    // Note(BZ) I'm not proud of this. Not sure what would be the correct way to access context from some transport layers running on Looper
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        fun getAppContext(): Context {
            return context
        }
    }
}