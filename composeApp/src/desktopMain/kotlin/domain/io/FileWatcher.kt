package domain.io

import kotlinx.coroutines.*
import java.nio.file.*

actual fun createFileWatcher(filePath: String, onFileChanged: () -> Unit): FileWatcher {
    return FileWatcherImpl(filePath, onFileChanged)
}

class FileWatcherImpl(private val filePath: String, private val onFileChanged: () -> Unit) : FileWatcher {
    private val watchService = FileSystems.getDefault().newWatchService()
    private val path = Paths.get(filePath).parent
    private val job: Job

    init {
        path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val key = watchService.take()
                key.pollEvents().forEach { event ->
                    val changedPath = event.context() as Path
                    if (changedPath.toString() == Paths.get(filePath).fileName.toString()) {
                        onFileChanged()
                    }
                }
                if (!key.reset()) {
                    break
                }
            }
        }
    }

    override fun watch() {
        // The watcher starts automatically in the init block
    }

    override fun close() {
        job.cancel()
        watchService.close()
    }
}