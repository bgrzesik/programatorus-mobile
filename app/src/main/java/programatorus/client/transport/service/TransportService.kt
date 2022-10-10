package programatorus.client.transport.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class TransportService : Service() {

    override fun onCreate() {
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}