package util

import domain.model.Preset
import domain.model.TextReplacement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

class FileProcessor(
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) {
    suspend fun processPreset(preset: Preset) = withContext(Dispatchers.IO) {
        val directory = preset.targetDirectory.toPath()
        if (!fileSystem.exists(directory)) {
            throw IllegalArgumentException("Directory does not exist: ${preset.targetDirectory}")
        }

        val files = findFiles(directory, preset.fileExtensions)
        files.forEach { file ->
            processFile(file, preset.replacements)
        }
    }

    private fun findFiles(directory: Path, extensions: List<String>): List<Path> {
        return fileSystem.listRecursively(directory)
            .filter { path ->
                extensions.any { ext ->
                    path.name.endsWith(ext, ignoreCase = true)
                }
            }
            .toList()
    }

    private fun processFile(file: Path, replacements: List<TextReplacement>) {
        val content = fileSystem.source(file).buffer().use { source ->
            source.readUtf8()
        }

        var modifiedContent = content
        replacements.forEach { replacement ->
            modifiedContent = modifiedContent.replace(
                Regex(replacement.searchPattern),
                replacement.replacement
            )
        }

        if (content != modifiedContent) {
            fileSystem.write(file, false) {
                writeUtf8(modifiedContent)
            }
        }
    }

    fun validateDirectory(path: String): Boolean {
        val directory = path.toPath()
        return fileSystem.exists(directory) && fileSystem.metadata(directory).isDirectory
    }

    fun getFileExtension(path: String): String? {
        val lastDot = path.lastIndexOf('.')
        return if (lastDot > 0) {
            path.substring(lastDot)
        } else {
            null
        }
    }
} 