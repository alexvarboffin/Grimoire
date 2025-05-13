package com.example.lib

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

fun main() {
    // Путь к Gradle cache
    val gradleCache = File(System.getProperty("user.home"), ".gradle/caches/modules-2/files-2.1")
    // Путь к целевому локальному maven-репозиторию
    //val localM2 = File("local-m2")
    val localM2 = File(System.getProperty("user.home"), ".m2/repository")
    gradleCache.walkTopDown().forEach { file ->
        if (file.isFile) {
            // parts: [group, artifact, version, hash, file]
            val parts = file.relativeTo(gradleCache).invariantSeparatorsPath.split("/")
            if (parts.size >= 5) {
                val group = parts[0].replace('.', '/')
                val artifact = parts[1]
                val version = parts[2]
                val fileName = parts[4]
                val target = File(localM2, "$group/$artifact/$version/$fileName")
                target.parentFile.mkdirs()

                    //println(file.toPath() + " " + target.toPath())



                try {
                    if (file.length() > 0) {
                        // Копируем только если размер больше 0
                        Files.copy(file.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        println("Скопировано: ${file.name}")
                    } else {
                        // Если размер 0 — удаляем целевой файл, если он есть
                        if (target.exists()) {
                            target.delete()
                            println("Удалён пустой файл: ${target.name}")
                        } else {
                            println("Пропущен пустой файл: ${file.name}")
                        }
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }
    println("Готово! Все зависимости скопированы в ${localM2.absolutePath}")
}