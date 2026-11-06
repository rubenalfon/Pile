package es.pile

import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL

class TestRedirect {
    @Test
    fun testRedirect() {
        val url = URL("https://huggingface.co/monkt/paddleocr-onnx/resolve/main/detection/v3/det.onnx")
        val connection = url.openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        connection.connect()
        println("Code: ${connection.responseCode}")
        println("Location: ${connection.getHeaderField("Location")}")

        val input = connection.inputStream
        val bytes = ByteArray(10)
        val read = input.read(bytes)
        println("Read bytes: $read")
    }
}
