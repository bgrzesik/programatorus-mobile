package programatorus.client.comm.transport

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel

class FrameDecoder(
    var mInputStream: InputStream
) {

    private var mEof = false
    private var mFrameBuffer = ByteBuffer.allocate(4096)

    fun readFrame(): ByteArray? {
        // TODO(bgrzesik): Don't read byte by byte
        var read : Int
        mFrameBuffer.clear()

        do {
            read = mInputStream.read()
            if (read == -1) {
                mEof = true
                return null
            }
        } while (read != 0)

        do {
            read = mInputStream.read()
            if (read == -1) {
                mEof = true
                return null
            }
        } while (read == 0)

        do {
            var sliceSize = read and 0x7f;

            read = mInputStream.read()
            if (read == 0 || read == -1) {
                // Error while parsing slice size
                return null
            }

            sliceSize = sliceSize or ((read and 0x7f) shl 7)

            for (i in 0 until sliceSize) {
                read = mInputStream.read()
                if (read == 0 || read == -1) {
                    break
                }
                mFrameBuffer.put(read.toByte())
            }

            if (read == 0) {
                break;
            }
            mFrameBuffer.put(0)

            read = mInputStream.read()
            if (read == 0 || read == -1) {
                break
            }
        } while (read != 0)

        mEof = read == -1

        val array = ByteArray(mFrameBuffer.position())
        mFrameBuffer.flip()
        mFrameBuffer.get(array)

        return array
    }

}