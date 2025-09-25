package domain.io

interface FileWatcher {
    fun watch()
    fun close()
}

expect fun createFileWatcher(filePath: String, onFileChanged: () -> Unit): FileWatcher
