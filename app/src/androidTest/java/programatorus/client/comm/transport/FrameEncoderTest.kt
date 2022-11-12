package programatorus.client.comm.transport

import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.*

class FrameEncoderTest {

    @Test(timeout=200)
    fun testWriteSliceSize() {
        fun testNumber(number: Int, expected: ByteArray) {
            val array = ByteArrayOutputStream(2);
            val frameEncoder = FrameEncoder(array)
            frameEncoder.writeSliceSize(number);
            Assert.assertArrayEquals(expected, array.toByteArray())
        }

        testNumber(0, byteArrayOf(0x80.toByte(), 0x80.toByte()))
        testNumber(10, byteArrayOf(0x8A.toByte(), 0x80.toByte()))
        testNumber(127, byteArrayOf(0xFF.toByte(), 0x80.toByte()))
        testNumber(128, byteArrayOf(0x80.toByte(), 0x81.toByte()))
        testNumber(129, byteArrayOf(0x81.toByte(), 0x81.toByte()))
        testNumber(256, byteArrayOf(0x80.toByte(), 0x82.toByte()))
        testNumber(512, byteArrayOf(0x80.toByte(), 0x84.toByte()))
        testNumber(1024, byteArrayOf(0x80.toByte(), 0x88.toByte()))
    }

    private fun testEncoding(data: ByteArray, expected: ByteArray) {
        val array = ByteArrayOutputStream(10);
        val frameEncoder = FrameEncoder(array)

        frameEncoder.write(data)
        frameEncoder.flush()
        frameEncoder.finishFrame()

        Assert.assertArrayEquals(expected, array.toByteArray())
    }

    @Test(timeout=200)
    fun testWriteWithoutZero() {
        testEncoding(
            byteArrayOf(0x1, 0x2, 0x3, 0x4),
            byteArrayOf(0x85.toByte(), 0x80.toByte(), 0x1, 0x2, 0x3, 0x4, 0x0),
        )

        testEncoding(
            byteArrayOf(0x1, 0x2, 0x3, 0x4, 0x5, 0x6),
            byteArrayOf(0x87.toByte(), 0x80.toByte(), 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x0),
        )

        var long = ByteArray(256)
        Arrays.fill(long, 0x1a)

        testEncoding(
            long,
            byteArrayOf(0x81.toByte(), 0x82.toByte(), *long, 0x0)
        )
    }

    @Test(timeout=200)
    fun testWriteWithZero() {
        testEncoding(byteArrayOf(0x0), byteArrayOf(0x80.toByte(), 0x80.toByte(), 0x0))
        testEncoding(byteArrayOf(0x1, 0x0), byteArrayOf(0x81.toByte(), 0x80.toByte(), 0x1, 0x0))
        testEncoding(
            byteArrayOf(0x1, 0x0, 0x1),
            byteArrayOf(0x81.toByte(), 0x80.toByte(), 0x1, 0x82.toByte(), 0x80.toByte(), 0x1, 0x0)
        )
        testEncoding(
            byteArrayOf(0x1, 0x0, 0x1, 0x0),
            byteArrayOf(0x81.toByte(), 0x80.toByte(), 0x1, 0x81.toByte(), 0x80.toByte(), 0x1, 0x0)
        )
    }

}