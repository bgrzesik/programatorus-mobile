package programatorus.client.comm.transport.net

import android.util.Log
import programatorus.client.comm.transport.AbstractTransportBuilder
import programatorus.client.comm.transport.ITransport
import programatorus.client.comm.transport.ITransportClient
import programatorus.client.comm.transport.io.StreamingTransport
import programatorus.client.utils.TaskRunner
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class TcpTransport(
    private var mHost: String,
    private var mPort: Int,
    client: ITransportClient,
) : StreamingTransport<TcpTransport>(client) {

    companion object {
        private const val TAG = "TcpTransport"
    }

    private var mSocket: Socket? = null

    override var inputStream: InputStream? = null
        private set
    override var outputStream: OutputStream? = null
        private set

    override val isConnected: Boolean
        get() = (mSocket != null && inputStream != null && outputStream != null) && mSocket!!.isConnected

    override val disconnectOnReconnect: Boolean
        get() = true

    override fun doConnect(): Boolean {
        try {
            Log.i(TAG, "Creating TCP socket")
            mSocket = Socket(mHost, mPort)
            inputStream = mSocket?.inputStream
            outputStream = mSocket?.outputStream

            Log.i(TAG, "Created TCP socket")
        } catch (ex: IOException) {
            Log.e(TAG, "Connection failed", ex)
        }

        Log.e(TAG, "isConnected=${isConnected}")
        return isConnected
    }

    override fun doDisconnect() {
        inputStream?.close()
        inputStream = null

        outputStream?.close()
        outputStream = null
    }

    override fun toString(): String = "TcpTransport"

    class Builder : AbstractTransportBuilder<Builder>() {
        private var mHost: String? = null
        private var mPort: Int? = null

        fun setHost(host: String): Builder {
            mHost = host
            return this
        }

        fun setPort(port: Int): Builder {
            mPort = port
            return this
        }

        override fun construct(
            client: ITransportClient,
            taskRunner: TaskRunner,
            clientTaskRunner: TaskRunner
        ): ITransport = TcpTransport(mHost!!, mPort!!, client)
    }
}
