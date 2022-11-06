package programatorus.client.comm.transport.io

import android.os.Handler
import android.util.Log
import programatorus.client.comm.transport.AbstractTransportBuilder
import programatorus.client.comm.transport.ITransport
import programatorus.client.comm.transport.ITransportClient
import java.io.InputStream
import java.io.OutputStream

/**
 * Mostly for testing purposes
 */
class IOStreamTransport private constructor(
    client: ITransportClient,
    override val inputStream: InputStream,
    override val outputStream: OutputStream,
) : StreamingTransport<IOStreamTransport>(client) {
    companion object {
        const val TAG = "IOStreamTransport"
    }

    override val disconnectOnReconnect: Boolean
        get() = wasConnected

    override var isConnected: Boolean = false
    var wasConnected: Boolean = false

    override fun doConnect(): Boolean {
        if (wasConnected) {
            return false
        }
        isConnected = true
        wasConnected = true
        return true
    }

    override fun doDisconnect() {
        Log.d(TAG, "doDisconnect()")
        if (isConnected) {
            inputStream.close()
            outputStream.close()
            isConnected = false
        }
    }

    override fun toString(): String = "IOStreamTransport"

    class Builder : AbstractTransportBuilder<Builder>() {
        var mInputStream: InputStream? = null
        var mOutputStream: OutputStream? = null

        fun setInputStream(inputStream: InputStream): Builder {
            mInputStream = inputStream
            return this
        }

        fun setOutputStream(outputStream: OutputStream): Builder {
            mOutputStream = outputStream
            return this
        }

        override fun construct(
            client: ITransportClient,
            handler: Handler,
            clientHandler: Handler
        ): ITransport = IOStreamTransport(client, mInputStream!!, mOutputStream!!)
    }
}