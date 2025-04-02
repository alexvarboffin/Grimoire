import java.io.File
import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import kotlinx.serialization.Serializable


/*
*
* TOOLS DEPENDENCY MERGER
* implementation("com.akuleshov7:ktoml-core:0.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.0")
*
* */


@Serializable
data class TomlConfig(
    val versions: Map<String, String> = emptyMap(),
    val libraries: Map<String, Map<String, Map<String, Map<String, Long>>>> = emptyMap(), // Изменено для поддержки вложенных таблиц
    val plugins: Map<String, Map<String, Map<String, Map<String, Long>>>> = emptyMap()
)


fun main(args: Array<String>) {
    mergeDependencies(args)
}

fun mergeDependencies(args: Array<String>) {
    try {
        val sourceFile =
            if (args.isNotEmpty()) File(args[0])
            else File("C:\\android\\SRC\\WalhallaUI\\gradle\\libs.versions.toml")
        val targetFile = if (args.size > 1) File(args[1])
        else File("G:\\android\\FireTV0\\gradle\\xx.toml")
        val targetFile1 = if (args.size > 1) File(args[1])
        else File("G:\\android\\FireTV0\\gradle\\libs.versions.toml")

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


                // Сбор используемых версий из libraries
                val usedVersions = mutableSetOf<String>()
                usedVersions.addAll(getUsedLibraries(sourceToml.libraries))
                usedVersions.addAll(getUsedLibraries1(sourceToml.plugins))


                println("@@@@@@@@@@@ ${targetDeps.keys} ${sourceToml.libraries.size}")

                //throw Exception("Failed to write TOML content")


                val x: MutableMap<String, String> = mutableMapOf()
                x.put("compileSdk", "35")
                x.put("minSdk", "24")
                x.put("targetSdk", "35")
                x.put("buildTools", "35.0.0")


                // Удаление неиспользуемых версий из targetDeps
                val unusedVersions = targetDeps.keys.filter { it !in usedVersions }
                for (unusedVersion in unusedVersions) {
                    println("🗑️ Удалена неиспользуемая версия: $unusedVersion = ${targetDeps[unusedVersion]}")

                    if(unusedVersion !in x.keys) {
                        targetDeps.remove(unusedVersion)
                        println("Удалена неиспользуемая версия из targetDeps: $unusedVersion")
                    }

                    changed = true
                }


                val tomlContent = buildString {
                    appendLine("[versions]")
                    targetDeps.toSortedMap().forEach { (key, value) ->

                        //@@@@
                        appendLine("$key = \"$value\"")
                    }
                    // Добавляем секции libraries и plugins
                    appendLine("[libraries]")
                    sourceToml.libraries.toSortedMap().forEach { (libName, libConfig) ->
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

                                if (entry.key == "name") {
                                    println("NAME ${entry.value}")
                                }

                                append(" ${entry.key} = \"${entry.value}\"${if (index < entries.size - 1) "," else ""}")
                            }
                        }
                        append("}\n")
                    }
                    appendLine("[plugins]")


                    println("@@@@@ ${sourceToml.plugins.size}")
//@@@@
                    targetToml.plugins.toSortedMap().forEach { (libName, libConfig) ->
                        append("$libName = {")
                        val entries = libConfig.entries.toList()
                        entries.forEachIndexed { index, entry ->
                            if (entry.value is LinkedHashMap) {
                                var o = entry.key
                                //println(entry.key)
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


                println("Before 1: ${sourceToml.versions.size} | ${sourceToml.libraries.size} | ${sourceToml.plugins.size} ")
                println("Before 2: ${targetToml.versions.size} | ${targetToml.libraries.size} | ${targetToml.plugins.size} ")


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

fun getUsedLibraries1(plugins: Map<String, Map<String, Map<String, Map<String, Long>>>>): Collection<String> {
    val usedVersions = mutableSetOf<String>()
    plugins.forEach { (_, libConfig) ->
        val entries = libConfig.entries.toList()
        entries.forEachIndexed { index, entry ->
            if (entry.value is LinkedHashMap) {
                entry.value.entries.forEachIndexed { innerIndex, innerEntry ->

                    if (innerEntry.key == "ref") {
                        println(innerEntry)
                        usedVersions.add(innerEntry.value.toString())
                    } else {

                    }
                }
            } else {
                println("@@@@ ${entry.key}")
                if (entry.key == "name") {
                    usedVersions.add(entry.value.toString())
                }
            }
        }
    }
    return usedVersions
}

fun getUsedLibraries(libraries: Map<String, Map<String, Map<String, Map<String, Long>>>>): Collection<String> {

    val usedVersions = mutableSetOf<String>()

    libraries.forEach { (_, libConfig) ->
        val entries = libConfig.entries.toList()
        entries.forEachIndexed { index, entry ->
            if (entry.value is LinkedHashMap) {
                entry.value.entries.forEachIndexed { innerIndex, innerEntry ->
                    if (innerEntry.key == "ref") {
                        usedVersions.add(innerEntry.value.toString())
                    }
                }
            } else {
                if (entry.key == "name") {
                    usedVersions.add(entry.value.toString())
                }
            }
        }
    }
    return usedVersions
}

// Вспомогательная функция для извлечения ссылки на версию
fun extractVersionRef(value: String): String? {
    val versionRefPattern = """\{version.ref\s*=\s*["']([^"']+)["']\}""".toRegex()
    val match = versionRefPattern.find(value)
    return match?.groupValues?.getOrNull(1)
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
