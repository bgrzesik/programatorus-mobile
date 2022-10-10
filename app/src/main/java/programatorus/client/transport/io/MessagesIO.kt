package programatorus.client.transport.io

import programus.proto.GenericMessage
import java.io.*

object MessagesIO {
    // TODO(bgrzesik): Make sure it's sufficient
    private const val MAX_SIZE = 1024;


    fun writeMessage(message: GenericMessage, outputStream: OutputStream) {
        val bytes = message.toByteArray()

        // TODO(bgrzesik): Make sure that endianness is correct
        val dataOutputStream = DataOutputStream(outputStream)
        dataOutputStream.writeInt(bytes.size)
        dataOutputStream.write(bytes)
    }

    fun readMessage(inputStream: InputStream): GenericMessage? {
        val dataInputStream = DataInputStream(inputStream)
        val size = dataInputStream.readInt()

        if (size > MAX_SIZE) {
            throw IOException("Too big message requested")
        }

        val buffer = ByteArray(size)
        var pos = 0

        while (pos < size) {
            val read = inputStream.read(buffer, pos, size - pos)

            if (read == -1) {
                throw IOException("Unexpected EOF")
            }

            pos += read
        }

        return GenericMessage.parseFrom(buffer)
    }
}