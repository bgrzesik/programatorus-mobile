package programatorus.client.device

import android.net.Uri
import programatorus.client.model.Board
import java.util.concurrent.CompletableFuture

interface DeviceWrapper: IDevice {

    val wrappedDevice: IDevice

    override val isConnected: Boolean
        get() = wrappedDevice.isConnected

    override fun getBoards() = wrappedDevice.getBoards()

    override fun upload(documentUri: Uri) = wrappedDevice.upload(documentUri)

}