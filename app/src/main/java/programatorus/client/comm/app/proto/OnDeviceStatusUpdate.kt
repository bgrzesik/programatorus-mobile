package programatorus.client.comm.app.proto

import com.google.protobuf.Empty
import programatorus.client.comm.app.PojoResponder
import programus.proto.Protocol.DeviceUpdateStatus.Status.*
import programus.proto.Protocol.GenericMessage
import java.util.*

interface OnDeviceStatusUpdate : PojoResponder<OnDeviceStatusUpdate.DeviceStatus, Unit> {

    override val requestPayloadCase
        get() = GenericMessage.PayloadCase.DEVICEUPDATESTATUS

    override fun unpackRequest(request: GenericMessage): DeviceStatus {
        val update = request.deviceUpdateStatus
        val status = when (update.status) {
            UNREACHABLE -> DeviceStatus.Status.UNREACHABLE
            READY -> DeviceStatus.Status.READY
            FLASHING -> DeviceStatus.Status.FLASHING
            ERROR -> DeviceStatus.Status.ERROR
            UNRECOGNIZED -> DeviceStatus.Status.UNREACHABLE
            null -> DeviceStatus.Status.UNREACHABLE
        }

        val flashingProgress = Optional.of(update.flashingProgress)
        val image = Optional.of(update.image)
        return DeviceStatus(status, flashingProgress, image)
    }

    override fun prepareResponse(response: Unit): GenericMessage.Builder =
        GenericMessage.newBuilder()
            .setOk(Empty.getDefaultInstance())

    data class DeviceStatus(
        val status: Status,
        val flashingProgress: Optional<Float>,
        val image: Optional<String>,
    ) {
        enum class Status {
            UNREACHABLE,
            READY,
            FLASHING,
            ERROR,
        }
    }

}