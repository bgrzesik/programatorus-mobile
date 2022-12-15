package programatorus.client.comm.transport

import java.io.OutputStream
import java.nio.ByteBuffer

class FrameEncoder(
    val mOutputStream: OutputStream
) : OutputStream() {

    private val mSliceBuffer = ByteBuffer.allocate(4096*16)

    override fun write(b: Int) {
        if (b != 0) {
            mSliceBuffer.put(b.toByte())
        } else {
            writeSlice(false)
        }
    }

    fun writeSliceSize(size: Int) {
        var number = size;
        assert(number <= 0x7fff)
        mOutputStream.write(0x80 or (number and 0x7f))
        number = number ushr 7
        mOutputStream.write(0x80 or (number and 0x7f))
    }

    fun startFrame() {
        reset()
        mOutputStream.write(0);
    }

    private fun writeSlice(eof: Boolean) {
        writeSliceSize(mSliceBuffer.position() + (if (eof) 1 else 0))
        mOutputStream.write(mSliceBuffer.array(), 0, mSliceBuffer.position())
        reset()
    }

    fun finishFrame() {
        if (mSliceBuffer.position() > 0) {
            writeSlice(true)
        }
        mOutputStream.write(0);
        reset()
    }

    override fun flush() = mOutputStream.flush()

    override fun close() = mOutputStream.close()

    fun reset() {
        mSliceBuffer.clear()
    }
}