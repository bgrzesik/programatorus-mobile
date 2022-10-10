package programatorus.client.transport.io

import programatorus.client.transport.ITransportClient
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor

/**
 * Mostly for testing purposes
 */
class LoopbackTransport(
    client: ITransportClient,
    executor: ScheduledExecutorService = ScheduledThreadPoolExecutor(1)
) : IOTransport<LoopbackTransport>(client, executor) {

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