package programatorus.client.comm.transport.io

import android.os.Handler
import android.os.Looper
import android.util.Log
import programatorus.client.comm.transport.ITransportClient
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

/**
 * Mostly for testing purposes
 */
class PipedTransport(
    client: ITransportClient,
    executor: Handler = Handler(Looper.getMainLooper())
) : StreamingTransport<PipedTransport>(client, executor) {
    companion object {
        const val TAG = "PipedTransport"
    }

    override var inputStream: InputStream? = null

    override var outputStream: OutputStream? = null

    override val isConnected: Boolean
        get() = inputStream != null && outputStream != null

    override fun doConnect(): Boolean {
        Log.d(TAG, "doConnect()")
        val pipedOutputStream = PipedOutputStream()
        val pipedInputStream = PipedInputStream()

        pipedInputStream.connect(pipedOutputStream)

        outputStream = pipedOutputStream
        inputStream = pipedInputStream

        return true
    }

    override fun doDisconnect() {
        Log.d(TAG, "doConnect()")
        inputStream?.close()
        inputStream = null

        outputStream?.close()
        outputStream = null
    }

}