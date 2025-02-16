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
    data class ProcessingStats(
        val processedFiles: Int = 0,
        val totalReplacements: Int = 0,
        val fileStats: Map<String, Int> = emptyMap() // файл -> количество замен
    )

    suspend fun processPreset(preset: Preset): ProcessingStats = withContext(Dispatchers.IO) {
        val directory = preset.targetDirectory.toPath()
        if (!fileSystem.exists(directory)) {
            throw IllegalArgumentException("Directory does not exist: ${preset.targetDirectory}")
        }

        var stats = ProcessingStats()
        val files = findFiles(directory, preset.fileExtensions)
        
        files.forEach { file ->
            val replacementsCount = processFile(file, preset.replacements)
            if (replacementsCount > 0) {
                stats = stats.copy(
                    processedFiles = stats.processedFiles + 1,
                    totalReplacements = stats.totalReplacements + replacementsCount,
                    fileStats = stats.fileStats + (file.name to replacementsCount)
                )
            }
        }
        
        stats
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

    private fun processFile(file: Path, replacements: List<TextReplacement>): Int {
        // Читаем содержимое файла
        val content = fileSystem.source(file).buffer().use { source ->
            source.readUtf8()
        }

        var modifiedContent = content
        var totalReplacements = 0

        // Применяем каждую замену по очереди
        replacements.forEach { replacement ->
            // Выполняем замену и получаем статистику
            val replacementResult = when {
                replacement.isRegex -> performRegexReplacement(modifiedContent, replacement)
                else -> performSimpleReplacement(modifiedContent, replacement)
            }
            
            // Обновляем текст и счетчик замен
            modifiedContent = replacementResult.newContent
            totalReplacements += replacementResult.replacementCount
        }

        // Сохраняем изменения только если текст был модифицирован
        if (content != modifiedContent) {
            fileSystem.write(file, false) {
                writeUtf8(modifiedContent)
            }
        }

        return totalReplacements
    }

    private data class ReplacementResult(
        val newContent: String,
        val replacementCount: Int
    )

    private fun performRegexReplacement(content: String, replacement: TextReplacement): ReplacementResult {
        val regex = Regex(replacement.searchPattern)
        var count = 0
        
        // Заменяем с подсчетом количества замен
        val newContent = regex.replace(content) { matchResult ->
            count++
            var result = replacement.replacement
            
            // Заменяем все группы захвата ($1, $2, $3, ...) на соответствующие значения
            matchResult.groupValues.forEachIndexed { index, value ->
                if (index > 0) { // пропускаем нулевую группу, так как она содержит весь матч
                    result = result.replace("\$$index", value)
                }
            }
            
            result
        }
        
        return ReplacementResult(newContent, count)
    }

    private fun performSimpleReplacement(content: String, replacement: TextReplacement): ReplacementResult {
        val (newContent, count) = content.replaceWithCount(
            replacement.searchPattern,
            replacement.replacement
        )
        return ReplacementResult(newContent, count)
    }

    private fun String.replaceWithCount(searchPattern: String, replacement: String): Pair<String, Int> {
        var count = 0
        val parts = this.split(searchPattern)
        if (parts.size > 1) {
            count = parts.size - 1
        }
        return this.replace(searchPattern, replacement) to count
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