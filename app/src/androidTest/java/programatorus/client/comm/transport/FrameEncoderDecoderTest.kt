package programatorus.client.comm.transport

import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

class FrameEncoderDecoderTest {

    fun test(plain: ByteArray) {
        val arrayInput = ByteArrayOutputStream(4096);
        val frameEncoder = FrameEncoder(arrayInput)

        frameEncoder.finishFrame()
        frameEncoder.write(plain)
        frameEncoder.flush()
        frameEncoder.finishFrame()

        val arrayOutput = ByteArrayInputStream(arrayInput.toByteArray())
        val frameDecoder = FrameDecoder(arrayOutput)
        val decoded = frameDecoder.readFrame()
        Assert.assertNotNull(decoded)
        println(plain.contentToString())
        println(Arrays.toString(decoded))
        Assert.assertArrayEquals(plain, decoded)
    }

    @Test(timeout=200)
    fun testEncodeDecodeWithoutZeros() {
        test(byteArrayOf(0x1, 0x2, 0x3, 0x4))
        test(byteArrayOf(0x1, 0x2, 0x3, 0x4, 0x5, 0x6))
        test(byteArrayOf(0x1, 0x0))
        test(byteArrayOf(0x1))
        test(byteArrayOf(0x1, 0x2))
        test(byteArrayOf(0x1, 0x2, 0x3))

        val plain = ByteArray(256)
        plain.fill(0x01)
        test(plain)
    }

    @Test(timeout=200)
    fun testEncodeDecodeWithZeros() {
        test(byteArrayOf(0x1, 0x0))
        test(byteArrayOf(0x1, 0x0, 0x1))
        test(byteArrayOf(0x1, 0x0, 0x1, 0x0))
        test(byteArrayOf(0x0))
        test(byteArrayOf(0x0, 0x0))
        test(byteArrayOf(0x0, 0x1, 0x0))
        test(ByteArray(256))
    }

    @Test(timeout=400)
    fun testRandomizedMessages() {
        val random = Random(2137)

        var bytes = ByteArray(20)
        random.nextBytes(bytes)
        test(bytes)

        bytes = ByteArray(100)
        random.nextBytes(bytes)
        test(bytes)

        bytes = ByteArray(512)
        random.nextBytes(bytes)
        test(bytes)

        bytes = ByteArray(512)
        random.nextBytes(bytes)
        test(bytes)

        bytes = ByteArray(1020)
        random.nextBytes(bytes)
        test(bytes)

        bytes = ByteArray(1020)
        random.nextBytes(bytes)
        test(bytes)

        bytes = ByteArray(2048)
        random.nextBytes(bytes)
        test(bytes)
    }
}