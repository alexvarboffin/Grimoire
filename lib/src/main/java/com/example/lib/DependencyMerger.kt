import java.io.File
import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import kotlinx.serialization.Serializable

@Serializable
data class TomlConfig(
    val versions: Map<String, String> = emptyMap(),
    val libraries: Map<String, Map<String, Map<String, Map<String, Long>>>> = emptyMap(), // Изменено для поддержки вложенных таблиц
    val plugins: Map<String, Map<String, Map<String, Map<String, Long>>>> = emptyMap()
)


fun main(args: Array<String>) {
    try {
        val sourceFile =
            if (args.isNotEmpty()) File(args[0])
            else File("G:\\android\\Landing\\gradle\\libs.versions.toml")
        val targetFile = if (args.size > 1) File(args[1])
        else File("D:\\walhalla\\AE\\gradle\\libs.versions.toml")
        val targetFile1 = if (args.size > 1) File(args[1])
        else File("D:\\walhalla\\AE\\gradle\\libs.versions-2.toml")

        println("🔍 Исходный файл: ${sourceFile.absolutePath}")
        println("📝 Целевой файл: ${targetFile.absolutePath}")

        // Проверяем наличие файлов
        if (!sourceFile.exists()) throw IllegalArgumentException("Исходный файл не найден")
        if (!targetFile.exists()) throw IllegalArgumentException("Целевой файл не найден")

        // Проверяем права доступа
        if (!sourceFile.canRead()) throw SecurityException("Нет прав на чтение исходного файла")
        if (!targetFile.canWrite()) throw SecurityException("Нет прав на запись в целевой файл")

        // Создаем экземпляр Toml с конфигурацией
        val toml = Toml(TomlInputConfig(ignoreUnknownNames = true))

        // Читаем TOML файлы
        val sourceToml = try {
            toml.decodeFromString(
                TomlConfig.serializer(),
                sourceFile.readText()
            )
        } catch (e: Exception) {
            throw IllegalStateException("Ошибка при чтении исходного TOML: ${e.message}")
        }

        val targetToml = try {
            toml.decodeFromString(
                TomlConfig.serializer(),
                targetFile.readText()
            )
        } catch (e: Exception) {
            throw IllegalStateException("Ошибка при чтении целевого TOML: ${e.message}")
        }

        // Получаем секции с версиями, библиотеками и плагинами
        val sourceDeps = sourceToml.versions
        val targetDeps = targetToml.versions.toMutableMap()

        // Добавляем недостающие зависимости и обновляем существующие
        var changed = false
        var addedCount = 0
        sourceDeps.forEach { (key, version1) ->
            if (key !in targetDeps) {
                targetDeps[key] = version1
                println("✅ Добавлена версия: $key = $version1")
                changed = true
                addedCount++
            } else {
                // Если зависимость уже существует, обновляем её версию
                if (targetDeps[key] != version1) {


                    val result = compareTo(version1, targetDeps[key] ?: "")

                    println("🔄 Обновлена версия: $key = $version1 ${targetDeps[key]} $result")
                    targetDeps[key] = version1
                    changed = true
                }
            }
        }

        // Сохраняем обновленный TOML
        if (changed) {
            try {
                val tomlContent = buildString {
                    appendLine("[versions]")
                    targetDeps.forEach { (key, value) ->
                        appendLine("$key = \"$value\"")
                    }
                    // Добавляем секции libraries и plugins
                    appendLine("[libraries]")
                    sourceToml.libraries.forEach { (libName, libConfig) ->
                        append("$libName = {")
                        val entries = libConfig.entries.toList()
                        entries.forEachIndexed { index, entry ->
                            if (entry.value is LinkedHashMap) {
                                var o = entry.key

                                entry.value.entries.forEachIndexed { innerIndex, innerEntry ->
                                    o += "." + innerEntry.key
                                    append(" $o = \"${innerEntry.value}\"${if ((innerIndex < entry.value.entries.size - 1) && index < entries.size - 1) "," else ","}")
                                }
                            } else {
                                append(" ${entry.key} = \"${entry.value}\"${if (index < entries.size - 1) "," else ""}")
                            }
                        }
                        append("}\n")
                    }
                    appendLine("[plugins]")
                    sourceToml.plugins.forEach { (libName, libConfig) ->
                        append("$libName = {")
                        val entries = libConfig.entries.toList()
                        entries.forEachIndexed { index, entry ->
                            if (entry.value is LinkedHashMap) {
                                var o = entry.key

                                entry.value.entries.forEachIndexed { innerIndex, innerEntry ->
                                    o += "." + innerEntry.key
                                    append(" $o = \"${innerEntry.value}\"${if ((innerIndex < entry.value.entries.size - 1) && index < entries.size - 1) "," else ","}")
                                }
                            } else {
                                append(" ${entry.key} = \"${entry.value}\"${if (index < entries.size - 1) "," else ""}")
                            }
                        }
                        append("}\n")
                    }
                }
                targetFile1.writeText(tomlContent)
                println("🎉 Файл успешно обновлен! Добавлено версий: $addedCount")
            } catch (e: Exception) {
                throw IllegalStateException("Ошибка при сохранении TOML: ${e.message}")
            }
        } else {
            println("ℹ️ Новых версий не найдено")
        }
    } catch (e: Exception) {
        println("❌ Ошибка: ${e.message}")
        System.exit(1)
    }
}

fun compareTo(version1: String, s: String): String {
    val result = version1.compareTo(s)

    return when {
        result < 0 -> {
            println("$version1 меньше ${s}")
            s
        }

        result > 0 -> {
            println("$version1 больше ${s}")
            version1
        }

        else -> {
            println("$version1 равно ${s}")
            version1
        }
    }
}
