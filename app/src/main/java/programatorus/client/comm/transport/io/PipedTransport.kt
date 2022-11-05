package programatorus.client.comm.transport.io

import android.os.Handler
import android.util.Log
import programatorus.client.comm.transport.AbstractTransportBuilder
import programatorus.client.comm.transport.ITransport
import programatorus.client.comm.transport.ITransportClient
import programatorus.client.comm.transport.bt.BluetoothTransport
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

/**
 * Mostly for testing purposes
 */
class PipedTransport(
    client: ITransportClient,
) : StreamingTransport<PipedTransport>(client) {
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

    override fun toString(): String = "PipedTransport"


    class Builder : AbstractTransportBuilder<Builder>() {
        override fun construct(
            client: ITransportClient,
            handler: Handler,
            clientHandler: Handler
        ): ITransport = PipedTransport(client)
    }
}