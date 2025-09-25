package domain.io

import java.io.File

actual class FileSystem {
    actual fun listFiles(path: String): List<FileEntry> {
        val directory = File(path)
        if (!directory.isDirectory) {
            return emptyList()
        }
        return directory.listFiles()?.map { file ->
            FileEntry(
                name = file.name,
                isDirectory = file.isDirectory,
                path = file.absolutePath
            )
        } ?: emptyList()
    }
}