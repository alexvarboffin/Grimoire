package domain.dsstore

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

class DSStoreParser {

    private lateinit var buffer: ByteBuffer
    private val records = mutableListOf<Record>()
    private lateinit var offsets: List<Int>
    private var rootNodeId: Int = 0

    fun parse(data: ByteArray): List<Record> {
        buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN)
        records.clear()

        val allocatorOffset = parseHeader()
        parseAllocator(allocatorOffset)
        parseTree(nodeId = rootNodeId)

        return records
    }

    private fun parseHeader(): Int {
        val alignment = buffer.int
        if (alignment != 0x00000001) {
            // warnings.warn(f'Alignment int {hex(alignment)} not 0x00000001')
        }

        val magic = buffer.int
        if (magic != 0x42756431) { // Bud1
            // warnings.warn(f'Magic bytes {hex(magic)} not 0x42756431 (Bud1)')
        }

        val allocatorOffset = 0x4 + buffer.int
        val allocatorLength = buffer.int
        val allocatorOffsetRepeat = 0x4 + buffer.int
        if (allocatorOffsetRepeat != allocatorOffset) {
            // warnings.warn(f'Allocator offsets {hex(self.allocator_offset)} and'
            // f' {hex(allocator_offset_repeat)} unequal')
        }

        buffer.position(allocatorOffset)
        return allocatorOffset
    }

    private fun parseAllocator(allocatorOffset: Int) {
        val numOffsets = buffer.int
        val second = buffer.int
        if (second != 0) {
            // warnings.warn(f'Second int of allocator {hex(second)}'
            // ' not 0x00000000')
        }
        offsets = List(numOffsets) { buffer.int }
        println("Offsets: ${offsets.joinToString { "0x" + it.toString(16) }}")

        buffer.position(allocatorOffset + 0x408)

        val numKeys = buffer.int
        val directory = mutableMapOf<String, Int>()
        for (i in 0 until numKeys) {
            val keyLength = buffer.get().toInt()
            val key = readString(keyLength)
            directory[key] = buffer.int
        }
        println("Directory: $directory")

        rootNodeId = directory["DSDB"] ?: throw IllegalStateException("DSDB key not found in directory")
    }

    private fun parseTree(nodeId: Int, master: Boolean = true) {
        println("Parsing tree node: $nodeId, master: $master, position: ${buffer.position()}")
        val offsetAndSize = offsets[nodeId]
        val offset = 0x4 + (offsetAndSize ushr 5 shl 5)
        println("  offsetAndSize for nodeId $nodeId: 0x${offsetAndSize.toString(16)}, calculated offset: 0x${offset.toString(16)}")
        buffer.position(offset)
        println("  Moved to position: ${buffer.position()}")

        if (master) {
            val rootId = buffer.int
            val treeHeight = buffer.int
            val numRecords = buffer.int
            val numNodes = buffer.int
            val fifth = buffer.int
            println("  Master node: rootId=$rootId, treeHeight=$treeHeight, numRecords=$numRecords, numNodes=$numNodes")
            if (fifth != 0x00001000) {
                // warnings.warn(f'Fifth int of master {hex(fifth)}'
                // ' not 0x00001000')
            }
            parseTree(rootId, false)
        } else {
            val nextId = buffer.int
            val numRecords = buffer.int
            println("  Node: nextId=0x${nextId.toString(16)}, numRecords=$numRecords")
            for (i in 0 until numRecords) {
                println("    Record $i")
                if (nextId != 0) {
                    val childId = buffer.int
                    println("      Internal node, childId: 0x${childId.toString(16)}")
                    val currentPosition = buffer.position()
                    parseTree(childId, false)
                    buffer.position(currentPosition)
                }

                val nameLength = buffer.int
                val name = readUtf16BeString(nameLength)
                val field = readString(4)
                val data = parseData()
                println("      Record data: name=$name, field=$field")

                val existingRecord = records.find { it.name == name }
                if (existingRecord != null) {
                    existingRecord.update(mapOf(field to data))
                } else {
                    records.add(Record(name, mutableMapOf(field to data)))
                }
            }

            if (nextId != 0) {
                println("  Following nextId: 0x${nextId.toString(16)}")
                parseTree(nextId, false)
            }
        }
    }

    private fun parseData(): Any {
        val dataType = readString(4)
        return when (dataType) {
            "bool" -> buffer.get().toInt() and 0x01 != 0
            "shor", "long" -> buffer.int
            "comp", "dutc" -> buffer.long
            "type" -> readString(4)
            "blob" -> {
                val length = buffer.int
                readBytes(length)
            }
            "ustr" -> {
                val length = buffer.int
                readUtf16BeString(length)
            }
            else -> throw NotImplementedError("Unrecognized data type $dataType")
        }
    }

    private fun readString(length: Int): String {
        val bytes = readBytes(length)
        return String(bytes, StandardCharsets.US_ASCII)
    }

    private fun readUtf16BeString(length: Int): String {
        val bytes = readBytes(length * 2)
        return String(bytes, Charsets.UTF_16BE)
    }

    private fun readBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        buffer.get(bytes)
        return bytes
    }
}

data class Record(val name: String, val fields: MutableMap<String, Any>) {
    fun update(newFields: Map<String, Any>) {
        fields.putAll(newFields)
    }

    fun humanReadable(field: String, data: Any): String {
        return when (field) {
            "Iloc" -> {
                if (data is ByteArray && data.size == 16) {
                    val buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN)
                    val x = buffer.int
                    val y = buffer.int
                    "Icon location: x ${x}px, y ${y}px"
                } else {
                    data.toString()
                }
            }
            else -> data.toString()
        }
    }
}
