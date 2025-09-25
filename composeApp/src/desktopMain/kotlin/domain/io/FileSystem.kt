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

actual fun saveFile(path: String, content: String) {
    val file = java.io.File(path)
    file.parentFile?.mkdirs()
    file.writeText(content)
}

actual fun getParentPath(path: String): String? {
    return java.io.File(path).parent
}

actual fun readFileContent(path: String): String {
    return java.io.File(path).readText()
}

actual fun listFilesRecursively(path: String): List<FileEntry> {
    val root = java.io.File(path)
    if (!root.isDirectory) return emptyList()
    return root.walkTopDown().map { file ->
        FileEntry(
            name = file.name,
            isDirectory = file.isDirectory,
            path = file.absolutePath
        )
    }.toList()
}