import java.io.File

fun main() {
    val file = File(".DS_Store_test_2")
    val bytes = file.readBytes()
    println("val data = byteArrayOf(")
    println(bytes.joinToString(", ") { "0x%02x".format(it) })
    println(")")
}
