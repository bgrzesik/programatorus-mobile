package programatorus.client.device

import programatorus.client.model.Board
import java.util.concurrent.CompletableFuture

interface DeviceWrapper: IDevice {

    val wrappedDevice: IDevice

    override val isConnected: Boolean
        get() = wrappedDevice.isConnected

    override fun getBoards() = wrappedDevice.getBoards()

}