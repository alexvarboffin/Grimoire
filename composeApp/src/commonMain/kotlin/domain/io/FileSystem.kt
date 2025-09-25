package domain.io

expect class FileSystem {
    fun listFiles(path: String): List<FileEntry>
}

data class FileEntry(
    val name: String,
    val isDirectory: Boolean,
    val path: String
)