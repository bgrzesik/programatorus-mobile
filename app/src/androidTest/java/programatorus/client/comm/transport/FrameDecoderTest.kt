package programatorus.client.comm.transport

import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.*

class FrameDecoderTest {

    private fun decodeSingleFrame(bytes: ByteArray): ByteArray? {
        val inputStream = ByteArrayInputStream(bytes)
        val frameDecoder = FrameDecoder(inputStream)
        return frameDecoder.readFrame()
    }

    private fun testSingleFrame(plain: ByteArray, byteStream: ByteArray) {
        val decoded = decodeSingleFrame(byteStream)
        Assert.assertNotNull(decoded)
        Assert.assertArrayEquals(plain, decoded)
    }

    @Test(timeout=200)
    fun testRubbishData() {
        Assert.assertNull(decodeSingleFrame(byteArrayOf(0xff.toByte(), 0xf1.toByte(), 0x2f, 0x3f)))
    }

    @Test(timeout=200)
    fun testEmptySteam() {
        Assert.assertNull(decodeSingleFrame(byteArrayOf()))
    }

    @Test(timeout=200)
    fun testNullSteam() {
        Assert.assertNull(decodeSingleFrame(byteArrayOf(0x00, 0x00)))
    }

    @Test(timeout=200)
    fun testSteamWithoutZeros() {
        testSingleFrame(
            byteArrayOf(0x1),
            byteArrayOf(0x00, 0x82.toByte(), 0x80.toByte(), 0x1, 0x0)
        )
        testSingleFrame(
            byteArrayOf(0x1, 0x2),
            byteArrayOf(0x00, 0x83.toByte(), 0x80.toByte(), 0x1, 0x2, 0x0)
        )
        testSingleFrame(
            byteArrayOf(0x1, 0x2, 0x3),
            byteArrayOf(0x00, 0x84.toByte(), 0x80.toByte(), 0x1, 0x2, 0x3, 0x0)
        )
    }

    @Test(timeout=200)
    fun testSteamWithZeros() {
        testSingleFrame(
            byteArrayOf(0x1, 0x0),
            byteArrayOf(0x00, 0x81.toByte(), 0x80.toByte(), 0x1, 0x0)
        )
        testSingleFrame(byteArrayOf(0x0), byteArrayOf(0x0, 0x80.toByte(), 0x80.toByte(), 0x0))
        testSingleFrame(
            byteArrayOf(0x1, 0x0),
            byteArrayOf(0x0, 0x81.toByte(), 0x80.toByte(), 0x1, 0x0)
        )
        testSingleFrame(
            byteArrayOf(0x1, 0x0, 0x1),
            byteArrayOf(
                0x0,
                0x81.toByte(),
                0x80.toByte(),
                0x1,
                0x82.toByte(),
                0x80.toByte(),
                0x1,
                0x0
            )
        )
        testSingleFrame(
            byteArrayOf(0x1, 0x0, 0x1, 0x0),
            byteArrayOf(
                0x0,
                0x81.toByte(),
                0x80.toByte(),
                0x1,
                0x81.toByte(),
                0x80.toByte(),
                0x1,
                0x0
            )
        )
    }

}