package programatorus.client.comm.transport.io

import android.os.Handler
import android.os.Looper
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

    override var inputStream: InputStream? = null

    override var outputStream: OutputStream? = null

    override val isConnected: Boolean
        get() = inputStream != null && outputStream != null

    override fun doConnect(): Boolean {
        val pipedOutputStream = PipedOutputStream()
        val pipedInputStream = PipedInputStream()

        pipedInputStream.connect(pipedOutputStream)

        outputStream = pipedOutputStream
        inputStream = pipedInputStream

        return true
    }

    override fun doDisconnect() {
        inputStream?.close()
        inputStream = null

        outputStream?.close()
        outputStream = null
    }

}