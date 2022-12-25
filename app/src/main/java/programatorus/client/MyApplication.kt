package programatorus.client

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class MyApplication: Application() {



    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        fun getAppContext(): Context {
            return context
        }
    }
}