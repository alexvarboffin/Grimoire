package data.local

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_3_5 = object : Migration(3, 5) {
    override fun migrate(connection: SQLiteConnection) {
        // Создаем таблицу пресетов команд
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS `command_presets` (
                `id` INTEGER PRIMARY KEY AUTOGenerate NOT NULL, 
                `groupName` TEXT NOT NULL, 
                `subGroupName` TEXT NOT NULL, 
                `name` TEXT NOT NULL, 
                `executablePath` TEXT NOT NULL, 
                `arguments` TEXT NOT NULL, 
                `workingDir` TEXT NOT NULL, 
                `description` TEXT NOT NULL
            )
        """.trimIndent())

        // Создаем таблицу истории
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS `command_history` (
                `id` INTEGER PRIMARY KEY AUTOGenerate NOT NULL, 
                `presetId` INTEGER NOT NULL, 
                `commandName` TEXT NOT NULL, 
                `fullCommand` TEXT NOT NULL, 
                `timestamp` INTEGER NOT NULL, 
                `exitCode` INTEGER, 
                `logs` TEXT NOT NULL, 
                `status` TEXT NOT NULL
            )
        """.trimIndent())

        // Создаем таблицу конвейеров
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS `command_pipelines` (
                `id` INTEGER PRIMARY KEY AUTOGenerate NOT NULL, 
                `name` TEXT NOT NULL, 
                `description` TEXT NOT NULL
            )
        """.trimIndent())

        // Создаем таблицу шагов конвейера
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS `pipeline_steps` (
                `id` INTEGER PRIMARY KEY AUTOGenerate NOT NULL, 
                `pipelineId` INTEGER NOT NULL, 
                `presetId` INTEGER NOT NULL, 
                `sequenceOrder` INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS `push_configs` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `name` TEXT NOT NULL, 
                `serviceAccountJsonPath` TEXT NOT NULL, 
                `createdAt` INTEGER NOT NULL
            )
        """.trimIndent())

        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS `push_devices` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `configId` INTEGER NOT NULL, 
                `name` TEXT NOT NULL, 
                `token` TEXT NOT NULL, 
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`configId`) REFERENCES `push_configs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """.trimIndent())

        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_push_devices_configId` ON `push_devices` (`configId`)")
    }
}
