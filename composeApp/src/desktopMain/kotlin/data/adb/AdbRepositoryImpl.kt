package data.adb

import domain.model.AndroidApp
import domain.model.AndroidComponent
import domain.model.AndroidDevice
import domain.model.ComponentType
import domain.model.IntentFilter
import domain.repository.AdbRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONArray


import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.io.path.Path


class AdbRepositoryImpl : AdbRepository {
    private val adbPath = findAdbPath()
    override val isRunning = MutableStateFlow(false)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    override suspend fun startAdbServer() {
        withContext(Dispatchers.IO) {
            try {
                executeCommand("$adbPath start-server")
                // Пробуем получить root права
                try {
                    executeCommand("$adbPath root")
                    println("Successfully started ADB with root permissions")
                } catch (e: Exception) {
                    println("Warning: Could not get root access: ${e.message}")
                }
                isRunning.value = true
            } catch (e: Exception) {
                throw Exception("Не удалось запустить ADB сервер: ${e.message}")
            }
        }
    }

    override suspend fun stopAdbServer() {
        withContext(Dispatchers.IO) {
            try {
                executeCommand("$adbPath kill-server")
                isRunning.value = false
            } catch (e: Exception) {
                throw Exception("Не удалось остановить ADB сервер: ${e.message}")
            }
        }
    }

    override suspend fun getDevices(): List<AndroidDevice> = withContext(Dispatchers.IO) {
        println("Getting connected devices...")
        val output = executeCommand("$adbPath devices -l")
        println("Raw devices output: $output")
        
        output.lines()
            .drop(1) // Skip "List of devices attached" header
            .filter { it.isNotBlank() }
            .map { line ->
                println("Processing device line: $line")
                val parts = line.split(Regex("\\s+"))
                if (parts.size < 2) {
                    println("Invalid device line format: $line")
                    return@map null
                }
                
                val id = parts[0]
                val isEmulator = id.startsWith("emulator-")
                
                val model = parts.find { it.startsWith("model:") }?.substringAfter("model:") ?: "Unknown"
                val product = parts.find { it.startsWith("product:") }?.substringAfter("product:") ?: "Unknown"
                
                println("Found device: id=$id, model=$model, product=$product, isEmulator=$isEmulator")
                
                AndroidDevice(
                    id = id,
                    model = model,
                    product = product,
                    isEmulator = isEmulator
                )
            }
            .filterNotNull()
            .also { devices ->
                println("Total devices found: ${devices.size}")
            }
    }

    override suspend fun getInstalledApps(deviceId: String): List<AndroidApp> {
        return withContext(Dispatchers.IO) {
            try {
                println("\nGetting installed apps for device: $deviceId")
                
                val output = executeCommand("$adbPath -s $deviceId shell pm list packages -f -u")
                println("\nParsing packages output...")
                
                output.lines()
                    .filter { it.startsWith("package:") }
                    .mapNotNull { line ->
                        try {
                            val packagePath = line.substringAfter("package:").substringBeforeLast("=")
                            val packageName = line.substringAfterLast("=")
                            
                            // Пробуем сначала получить информацию через клиент
                            val clientInfo = getAppInfoFromClient(deviceId, packageName)
                            if (clientInfo != null) {
                                println("Got app info from client for $packageName")
                                return@mapNotNull parseAppFromJson(clientInfo)
                            }
                            
                            println("Falling back to dumpsys for $packageName")
                            // Если не получилось - используем dumpsys
                            val dumpOutput = executeCommand("$adbPath -s $deviceId shell dumpsys package $packageName")
                            
                            // Parse version info
                            val versionName = "versionName=(\\S+)".toRegex()
                                .find(dumpOutput)?.groupValues?.get(1) ?: "unknown"
                            val versionCode = "versionCode=(\\d+)".toRegex()
                                .find(dumpOutput)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
                            
                            // Check if system app
                            val isSystemApp = dumpOutput.contains("System Package:") || 
                                            packagePath.startsWith("/system/")
                            
                            // Create JSON object with app info
                            val appJson = JSONObject().apply {
                                put("packageName", packageName)
                                put("appName", packageName.substringAfterLast("."))
                                put("versionName", versionName)
                                put("versionCode", versionCode)
                                put("isSystemApp", isSystemApp)
                                put("installTime", System.currentTimeMillis())
                                put("updateTime", System.currentTimeMillis())
                                
                                // Additional flags
                                put("isUpdatedSystemApp", dumpOutput.contains("updated-system"))
                                put("isDebugApp", dumpOutput.contains("DEBUGGABLE"))
                                put("isTestOnly", dumpOutput.contains("TEST_ONLY"))
                                put("hasInternetAccess", dumpOutput.contains("android.permission.INTERNET"))
                                put("allowsBackup", !dumpOutput.contains("ALLOW_BACKUP=false"))
                                put("hasNativeLibs", dumpOutput.contains("native-code"))
                                
                                // Parse permissions
                                val permissions = "requested permissions:([\\s\\S]*?)install permissions:".toRegex()
                                    .find(dumpOutput)?.groupValues?.get(1)?.lines()
                                    ?.filter { it.trim().startsWith("android.permission.") }
                                    ?.map { it.trim() }
                                    ?: emptyList()
                                put("permissions", JSONArray(permissions))
                                
                                // Parse SDK versions
                                "targetSdk=(\\d+)".toRegex()
                                    .find(dumpOutput)?.groupValues?.get(1)?.toIntOrNull()?.let {
                                        put("targetSdkVersion", it)
                                    }
                                "minSdk=(\\d+)".toRegex()
                                    .find(dumpOutput)?.groupValues?.get(1)?.toIntOrNull()?.let {
                                        put("minSdkVersion", it)
                                    }

                                // Parse components
                                parseComponents(dumpOutput, "Activity Resolver Table:", "activity").let {
                                    put("activities", JSONArray(it))
                                }
                                parseComponents(dumpOutput, "Service Resolver Table:", "service").let {
                                    put("services", JSONArray(it))
                                }
                                parseComponents(dumpOutput, "Receiver Resolver Table:", "receiver").let {
                                    put("receivers", JSONArray(it))
                                }
                                parseComponents(dumpOutput, "Provider Resolver Table:", "provider").let {
                                    put("providers", JSONArray(it))
                                }
                            }
                            
                            parseAppFromJson(appJson)
                        } catch (e: Exception) {
                            println("Error processing package: ${e.message}")
                            e.printStackTrace()
                            null
                        }
                    }
                    .also { apps ->
                        println("\nTotal apps processed: ${apps.size}")
                    }
            } catch (e: Exception) {
                println("Error getting apps: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }

    override suspend fun getAppIcon(deviceId: String, packageName: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val tempDir = System.getProperty("java.io.tmpdir")
                val tempFile = File(tempDir, "icon_$packageName.png")
                
                // Pull icon from device
                executeCommand("$adbPath -s $deviceId shell pm path $packageName").let { output ->
                    val apkPath = output.substringAfter("package:").trim()
                    executeCommand("$adbPath -s $deviceId pull $apkPath ${tempFile.absolutePath}")
                }
                
                if (tempFile.exists()) {
                    val bytes = tempFile.readBytes()
                    tempFile.delete()
                    bytes
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun uninstallApp(deviceId: String, packageName: String) {
        withContext(Dispatchers.IO) {
            executeCommand("$adbPath -s $deviceId uninstall $packageName")
        }
    }

    override suspend fun clearAppData(deviceId: String, packageName: String) {
        withContext(Dispatchers.IO) {
            executeCommand("$adbPath -s $deviceId shell pm clear $packageName")
        }
    }

    override suspend fun forceStopApp(deviceId: String, packageName: String) {
        withContext(Dispatchers.IO) {
            executeCommand("$adbPath -s $deviceId shell am force-stop $packageName")
        }
    }

    override suspend fun openInGooglePlay(deviceId: String, packageName: String) {
        withContext(Dispatchers.IO) {
            executeCommand("$adbPath -s $deviceId shell am start -a android.intent.action.VIEW -d market://details?id=$packageName")
        }
    }

    override suspend fun extractApk(deviceId: String, packageName: String): String {
        return withContext(Dispatchers.IO) {
            println("Extracting APK for package: $packageName from device: $deviceId")
            
            // Проверяем существование пакета
            val checkCommand = "$adbPath -s $deviceId shell pm list packages | grep $packageName"
            println("Checking if package exists: $checkCommand")
            try {
                val checkOutput = executeCommand(checkCommand)
                if (!checkOutput.contains(packageName)) {
                    throw Exception("Package $packageName not found on device $deviceId")
                }
            } catch (e: Exception) {
                throw Exception("Package $packageName not found on device $deviceId")
            }
            
            // Получаем путь к APK на устройстве
            val pathCommand = "$adbPath -s $deviceId shell pm path $packageName"
            println("Executing path command: $pathCommand")
            val pathOutput = executeCommand(pathCommand)
            
            if (!pathOutput.startsWith("package:")) {
                throw Exception("Could not find APK path for package $packageName")
            }
            
            val devicePath = pathOutput.substringAfter("package:").trim()
            println("Found APK path on device: $devicePath")
            
            // Создаем директорию на рабочем столе
            val desktopPath = System.getProperty("user.home") + File.separator + "Desktop"
            val apkDir = File(desktopPath, "extracted_apks")
            if (!apkDir.exists()) {
                println("Creating directory: ${apkDir.absolutePath}")
                apkDir.mkdirs()
            }
            
            // Получаем информацию о версии
            val dumpCommand = "$adbPath -s $deviceId shell dumpsys package $packageName"
            println("Executing dump command: $dumpCommand")
            val appInfo = executeCommand(dumpCommand)
            
            val versionName = appInfo.lineSequence()
                .find { it.contains("versionName=") }
                ?.substringAfter("versionName=")
                ?.substringBefore(" ")
                ?: "unknown"
            println("Found version name: $versionName")
            
            val fileName = "${packageName}_v${versionName}.apk"
            val targetPath = File(apkDir, fileName).absolutePath
            println("Target APK path: $targetPath")
            
            // Копируем APK
            val pullCommand = "$adbPath -s $deviceId pull \"$devicePath\" \"$targetPath\""
            println("Executing pull command: $pullCommand")
            executeCommand(pullCommand)
            
            println("APK successfully extracted to: $targetPath")
            targetPath
        }
    }

    override suspend fun selectDevice(deviceId: String) {
        withContext(Dispatchers.IO) {
            executeCommand("$adbPath -s $deviceId root")
            executeCommand("$adbPath -s $deviceId remount")
        }
    }

    override suspend fun executeShellCommand(command: String): String {
        return withContext(Dispatchers.IO) {
            executeCommand(command)
        }
    }

    override fun getLogcat(deviceId: String): Flow<String> = channelFlow {
        val process = Runtime.getRuntime().exec("$adbPath -s $deviceId logcat -v time")
        val reader = process.inputStream.bufferedReader()
        
        launch(Dispatchers.IO) {
            try {
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    trySend(line!!)
                }
            } catch (e: Exception) {
                // Ignore
            } finally {
                process.destroy()
            }
        }
        
        awaitClose {
            process.destroy()
        }
    }

    override suspend fun getDeviceInfo(deviceId: String): Map<String, String> = withContext(Dispatchers.IO) {
        val output = executeCommand("$adbPath -s $deviceId shell getprop")
        val props = mutableMapOf<String, String>()
        output.lines().forEach { line ->
            if (line.contains(": ")) {
                val key = line.substringBefore(":").trim('[', ']')
                val value = line.substringAfter(":").trim('[', ']', ' ')
                props[key] = value
            }
        }
        props
    }

    override suspend fun takeScreenshot(deviceId: String): ByteArray = withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("screenshot", ".png")
        try {
            executeCommand("$adbPath -s $deviceId shell screencap -p /sdcard/screenshot.png")
            executeCommand("$adbPath -s $deviceId pull /sdcard/screenshot.png ${tempFile.absolutePath}")
            executeCommand("$adbPath -s $deviceId shell rm /sdcard/screenshot.png")
            tempFile.readBytes()
        } finally {
            tempFile.delete()
        }
    }

    override suspend fun listFiles(deviceId: String, path: String): List<domain.model.RemoteFile> = withContext(Dispatchers.IO) {
        val output = executeCommand("$adbPath -s $deviceId shell ls -la $path")
        output.lines()
            .filter { it.isNotBlank() && !it.startsWith("total") }
            .mapNotNull { line ->
                try {
                    val parts = line.split(Regex("\\s+"))
                    if (parts.size < 7) return@mapNotNull null
                    
                    val permissions = parts[0]
                    val isDir = permissions.startsWith("d")
                    val size = parts[4].toLongOrNull() ?: 0L
                    val date = parts[5]
                    val time = parts[6]
                    val name = parts.subList(7, parts.size).joinToString(" ")
                    
                    if (name == "." || name == "..") return@mapNotNull null
                    
                    domain.model.RemoteFile(
                        name = name,
                        path = if (path.endsWith("/")) "$path$name" else "$path/$name",
                        isDirectory = isDir,
                        size = size,
                        lastModified = "$date $time",
                        permissions = permissions
                    )
                } catch (e: Exception) {
                    null
                }
            }
    }

    override suspend fun pullFile(deviceId: String, remotePath: String, localPath: String) {
        withContext(Dispatchers.IO) {
            executeCommand("$adbPath -s $deviceId pull \"$remotePath\" \"$localPath\"")
        }
    }

    override suspend fun pushFile(deviceId: String, localPath: String, remotePath: String) {
        withContext(Dispatchers.IO) {
            executeCommand("$adbPath -s $deviceId push \"$localPath\" \"$remotePath\"")
        }
    }

    override suspend fun deleteFile(deviceId: String, path: String) {
        withContext(Dispatchers.IO) {
            executeCommand("$adbPath -s $deviceId shell rm -rf \"$path\"")
        }
    }

    override suspend fun createDirectory(deviceId: String, path: String) {
        withContext(Dispatchers.IO) {
            executeCommand("$adbPath -s $deviceId shell mkdir -p \"$path\"")
        }
    }

    private fun findAdbPath(): String {
        println("Looking for ADB path...")
        
        // Try ANDROID_HOME environment variable
        System.getenv("ANDROID_HOME")?.let { androidHome ->
            println("Found ANDROID_HOME: $androidHome")
            val adbPath = "$androidHome/platform-tools/adb"
            if (File(adbPath).exists() || File("$adbPath.exe").exists()) {
                println("Found ADB at: $adbPath")
                return adbPath
            }
        }
        
        // Try PATH
        val path = System.getenv("PATH")
        println("Searching in PATH: $path")
        
        val adbName = if (System.getProperty("os.name").lowercase().contains("windows")) "adb.exe" else "adb"
        path.split(File.pathSeparator).forEach { dir ->
            val adbPath = File(dir, adbName)
            println("Checking path: ${adbPath.absolutePath}")
            if (adbPath.exists()) {
                println("Found ADB at: ${adbPath.absolutePath}")
                return adbPath.absolutePath
            }
        }
        
        throw Exception("ADB not found. Please make sure Android SDK platform-tools are installed and added to PATH")
    }

    private fun executeCommand(command: String): String {
        println("\n=== Executing command ===")
        println("Command: $command")
        
        val process = Runtime.getRuntime().exec(command)
        val output = process.inputStream.bufferedReader().readText()
        val error = process.errorStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        
        println("Exit code: $exitCode")
        if (output.isNotEmpty()) println("Output:\n$output")
        if (error.isNotEmpty()) println("Error:\n$error")
        println("=== Command execution completed ===\n")
        
        if (error.isNotEmpty()) {
            throw Exception("Command failed with error: $error")
        }
        
        return output
    }

    private suspend fun getAppInfoFromClient(deviceId: String, packageName: String): JSONObject? {
        return withContext(Dispatchers.IO) {
            try {
                println("\n=== Getting app info from client for $packageName ===")
                
                // Очищаем старый ответ
                executeCommand("$adbPath -s $deviceId shell rm /data/local/tmp/adb_client_response.txt")
                
                // Отправляем команду сервису
                val command = "$adbPath -s $deviceId shell am startservice -n com.walhalla.adbclient/.service.AdbClientService --ei type ${AdbClientService.MSG_GET_APP_INFO} --es package $packageName"
                println("Command: $command")
                executeCommand(command)
                
                // Даем время на обработку
                kotlinx.coroutines.delay(500)
                
                // Читаем ответ из файла
                println("Reading response from file...")
                val response = executeCommand("$adbPath -s $deviceId shell cat /data/local/tmp/adb_client_response.txt")
                println("Raw response: $response")
                
                if (response.contains("error")) {
                    println("Client returned error")
                    null
                } else {
                    try {
                        val json = JSONObject(response.trim())
                        println("Successfully parsed JSON response")
                        json
                    } catch (e: Exception) {
                        println("Error parsing JSON response: ${e.message}")
                        null
                    }
                }
            } catch (e: Exception) {
                println("Error communicating with client: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    private fun parseAppFromJson(json: JSONObject): AndroidApp {
        return AndroidApp(
            packageName = json.getString("packageName"),
            appName = json.getString("appName"),
            versionName = json.getString("versionName"),
            versionCode = json.getLong("versionCode"),
            isSystemApp = json.getBoolean("isSystemApp"),
            isUpdatedSystemApp = json.optBoolean("isUpdatedSystemApp", false),
            isDebugApp = json.optBoolean("isDebugApp", false),
            isTestOnly = json.optBoolean("isTestOnly", false),
            hasSslPinning = json.optBoolean("hasSslPinning", false),
            hasInternetAccess = json.optBoolean("hasInternetAccess", false),
            allowsBackup = json.optBoolean("allowsBackup", true),
            hasNativeLibs = json.optBoolean("hasNativeLibs", false),
            isEncryptionAware = json.optBoolean("isEncryptionAware", false),
            activities = json.optJSONArray("activities")?.let { array ->
                List(array.length()) { i -> parseComponent(array.getJSONObject(i)) }
            } ?: emptyList(),
            services = json.optJSONArray("services")?.let { array ->
                List(array.length()) { i -> parseComponent(array.getJSONObject(i)) }
            } ?: emptyList(),
            receivers = json.optJSONArray("receivers")?.let { array ->
                List(array.length()) { i -> parseComponent(array.getJSONObject(i)) }
            } ?: emptyList(),
            providers = json.optJSONArray("providers")?.let { array ->
                List(array.length()) { i -> parseComponent(array.getJSONObject(i)) }
            } ?: emptyList(),
            installTime = json.getLong("installTime"),
            updateTime = json.getLong("updateTime"),
            targetSdkVersion = json.optInt("targetSdkVersion", 0),
            minSdkVersion = json.optInt("minSdkVersion", 0)
        )
    }

    private fun parseComponent(json: JSONObject): AndroidComponent {
        return AndroidComponent(
            name = json.getString("name"),
            type = ComponentType.valueOf(json.getString("type")),
            isExported = json.optBoolean("exported", false),
            permission = json.optString("permission", null),
            enabled = json.optBoolean("enabled", true),
            directBootAware = json.optBoolean("directBootAware", false),
            process = json.optString("process", null),
            intentFilters = json.optJSONArray("intentFilters")?.let { array ->
                List(array.length()) { i -> parseIntentFilter(array.getJSONObject(i)) }
            } ?: emptyList()
        )
    }

    private fun parseIntentFilter(json: JSONObject): IntentFilter {
        return IntentFilter(
            actions = json.optJSONArray("actions")?.let { array ->
                List(array.length()) { array.getString(it) }
            } ?: emptyList(),
            categories = json.optJSONArray("categories")?.let { array ->
                List(array.length()) { array.getString(it) }
            } ?: emptyList(),
            dataSchemes = json.optJSONArray("dataSchemes")?.let { array ->
                List(array.length()) { array.getString(it) }
            } ?: emptyList()
        )
    }

    private fun parseComponents(dumpOutput: String, startMarker: String, type: String): List<JSONObject> {
        val components = mutableListOf<JSONObject>()
        val section = dumpOutput.substringAfter(startMarker, "")
            .substringBefore("Resolver Table:", "")
            .trim()

        val componentPattern = """
            (\S+) \{
              (?:enabled=\[(true|false)\])?
              (?:exported=\[(true|false)\])?
              (?:permission=\[([^\]]+)\])?
              (?:directBootAware=\[(true|false)\])?
              (?:process=\[([^\]]+)\])?
              (?:Filter:([^}]+))?
            \}
        """.trimIndent().toRegex()

        componentPattern.findAll(section).forEach { match ->
            val (name, enabled, exported, permission, directBoot, process, filterStr) = match.destructured
            
            val intentFilters = mutableListOf<JSONObject>()
            if (!filterStr.isNullOrEmpty()) {
                val filterPattern = """
                    Action: "(.*?)"
                    Category: "(.*?)"
                    Scheme: "(.*?)"
                """.trimIndent().toRegex()
                
                filterPattern.findAll(filterStr).forEach { filterMatch ->
                    val (action, category, scheme) = filterMatch.destructured
                    intentFilters.add(JSONObject().apply {
                        put("actions", JSONArray().apply { if (action.isNotEmpty()) put(action) })
                        put("categories", JSONArray().apply { if (category.isNotEmpty()) put(category) })
                        put("dataSchemes", JSONArray().apply { if (scheme.isNotEmpty()) put(scheme) })
                    })
                }
            }

            components.add(JSONObject().apply {
                put("name", name)
                put("type", type.uppercase())
                put("exported", exported == "true")
                put("enabled", enabled != "false")
                put("permission", permission.takeIf { it.isNotEmpty() })
                put("directBootAware", directBoot == "true")
                put("process", process.takeIf { it.isNotEmpty() })
                put("intentFilters", JSONArray(intentFilters))
            })
        }

        return components
    }
} 