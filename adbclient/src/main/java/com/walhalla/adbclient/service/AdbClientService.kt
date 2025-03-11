package com.walhalla.adbclient.service

import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.IBinder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import android.graphics.Color
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import androidx.annotation.RequiresApi
import com.walhalla.adbclient.MainActivity
import kotlinx.coroutines.withContext
import org.json.JSONArray
import android.content.pm.ApplicationInfo
import dalvik.system.DexFile


class AdbClientService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val messenger = Messenger(IncomingHandler())
    
    companion object {
        const val MSG_GET_APP_INFO = 1
        const val MSG_GET_ALL_APPS_INFO = 2
        const val MSG_GET_APP_ICON = 3
        
        const val EXTRA_PACKAGE_NAME = "package_name"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "AdbClientService"
        private const val TAG = "AdbClientService"
        const val EXTRA_RESPONSE = "response"
        private const val RESPONSE_FILE = "/data/local/tmp/adb_client_response.txt"
    }
    
    private fun log(message: String) {
        android.util.Log.d(TAG, message)
    }
    
    override fun onCreate() {
        super.onCreate()
        log("Service onCreate")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ADB Client Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Сервис для взаимодействия с ADB"
                enableLights(true)
                lightColor = Color.BLUE
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val pendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ByteBreakerz ADB Client")
            .setContentText("Сервис активен и готов к работе")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()
    }
    
    inner class IncomingHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            log("Received message: ${msg.what}")
            when (msg.what) {
                MSG_GET_APP_INFO -> {
                    val packageName = msg.data.getString(EXTRA_PACKAGE_NAME)
                    log("Getting info for package: $packageName")
                    if (packageName != null) {
                        scope.launch {
                            val appInfo = getAppInfo(packageName)
                            log("App info result: $appInfo")
                            val response = Message.obtain(null, msg.what)
                            response.data = Bundle().apply {
                                putString(EXTRA_RESPONSE, appInfo.toString())
                            }
                            msg.replyTo?.send(response)
                            log("Response sent for package: $packageName")
                        }
                    }
                }
                MSG_GET_ALL_APPS_INFO -> {
                    log("Getting info for all apps")
                    scope.launch {
                        val appsInfo = getAllAppsInfo()
                        log("All apps info result: $appsInfo")
                        val response = Message.obtain(null, msg.what)
                        response.data = Bundle().apply {
                            putString(EXTRA_RESPONSE, appsInfo.toString())
                        }
                        msg.replyTo?.send(response)
                        log("Response sent for all apps")
                    }
                }
                MSG_GET_APP_ICON -> {
                    val packageName = msg.data.getString(EXTRA_PACKAGE_NAME)
                    if (packageName != null) {
                        scope.launch {
                            val iconPath = saveAppIcon(packageName)
                            val response = Message.obtain(null, msg.what)
                            response.data = Bundle().apply {
                                putString(EXTRA_RESPONSE, iconPath)
                            }
                            msg.replyTo?.send(response)
                        }
                    }
                }
                else -> super.handleMessage(msg)
            }
        }
    }
    
    override fun onBind(intent: Intent): IBinder? {
        return messenger.binder
    }
    

    private fun getAppInfo(packageName: String): JSONObject {
        val pm = applicationContext.packageManager
        val appInfo = try {
            pm.getPackageInfo(packageName, 
                PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SERVICES or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_PERMISSIONS or
                PackageManager.GET_META_DATA or
                PackageManager.GET_CONFIGURATIONS or
                PackageManager.GET_SIGNING_CERTIFICATES
            )
        } catch (e: Exception) {
            return JSONObject().put("error", "Package not found: ${e.message}")
        }

        return JSONObject().apply {
            put("packageName", appInfo.packageName)
            put("appName", pm.getApplicationLabel(appInfo.applicationInfo).toString())
            put("versionName", appInfo.versionName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                put("versionCode", appInfo.longVersionCode)
            } else {
                put("versionCode", appInfo.versionCode.toLong())
            }
            put("isSystemApp", (appInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0)
            put("isUpdatedSystemApp", (appInfo.applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)
            put("isDebugApp", (appInfo.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0)
            put("isTestOnly", (appInfo.applicationInfo.flags and ApplicationInfo.FLAG_TEST_ONLY) != 0)
            put("hasInternetAccess", pm.checkPermission(android.Manifest.permission.INTERNET, packageName) == PackageManager.PERMISSION_GRANTED)
            put("allowsBackup", (appInfo.applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP) != 0)
            put("hasNativeLibs", appInfo.applicationInfo.nativeLibraryDir != null)
            put("installTime", appInfo.firstInstallTime)
            put("updateTime", appInfo.lastUpdateTime)
            put("targetSdkVersion", appInfo.applicationInfo.targetSdkVersion)
            put("minSdkVersion", appInfo.applicationInfo.minSdkVersion)

            // Permissions
            put("permissions", JSONArray().apply {
                appInfo.requestedPermissions?.forEach { permission ->
                    put(permission)
                }
            })

            // Activities
            put("activities", JSONArray().apply {
                appInfo.activities?.forEach { activity ->
                    put(JSONObject().apply {
                        put("name", activity.name)
                        put("type", "ACTIVITY")
                        put("exported", activity.exported)
                        put("permission", activity.permission)
                        put("enabled", activity.enabled)
                        put("directBootAware", activity.directBootAware)
                        put("process", activity.processName)
                        
                        // Intent Filters
                        put("intentFilters", JSONArray().apply {
                            try {
                                val activityInfo = pm.getActivityInfo(
                                    android.content.ComponentName(packageName, activity.name),
                                    PackageManager.GET_META_DATA
                                )
                                activityInfo.metaData?.let { metaData ->
                                    val filterActions = metaData.keySet()?.filter { it.startsWith("intent-filter.") }
                                    filterActions?.forEach { key ->
                                        val filter = JSONObject()
                                        // Actions
                                        val actions = metaData.getString("$key.actions")?.split(",")
                                        if (!actions.isNullOrEmpty()) {
                                            filter.put("actions", JSONArray(actions))
                                        }
                                        // Categories
                                        val categories = metaData.getString("$key.categories")?.split(",")
                                        if (!categories.isNullOrEmpty()) {
                                            filter.put("categories", JSONArray(categories))
                                        }
                                        // Data Schemes
                                        val schemes = metaData.getString("$key.data.scheme")?.split(",")
                                        if (!schemes.isNullOrEmpty()) {
                                            filter.put("dataSchemes", JSONArray(schemes))
                                        }
                                        put(filter)
                                    }
                                }
                            } catch (e: Exception) {
                                // Игнорируем ошибки при получении фильтров
                            }
                        })
                    })
                }
            })

            // Services
            put("services", JSONArray().apply {
                appInfo.services?.forEach { service ->
                    put(JSONObject().apply {
                        put("name", service.name)
                        put("type", "SERVICE")
                        put("exported", service.exported)
                        put("permission", service.permission)
                        put("enabled", service.enabled)
                        put("directBootAware", service.directBootAware)
                        put("process", service.processName)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put("foregroundServiceType", service.foregroundServiceType)
                        }
                    })
                }
            })

            // Receivers
            put("receivers", JSONArray().apply {
                appInfo.receivers?.forEach { receiver ->
                    put(JSONObject().apply {
                        put("name", receiver.name)
                        put("type", "RECEIVER")
                        put("exported", receiver.exported)
                        put("permission", receiver.permission)
                        put("enabled", receiver.enabled)
                        put("directBootAware", receiver.directBootAware)
                        put("process", receiver.processName)
                    })
                }
            })

            // Providers
            put("providers", JSONArray().apply {
                appInfo.providers?.forEach { provider ->
                    put(JSONObject().apply {
                        put("name", provider.name)
                        put("type", "PROVIDER")
                        put("exported", provider.exported)
                        //put("permission", provider.permission)
                        put("enabled", provider.enabled)
                        put("directBootAware", provider.directBootAware)
                        put("process", provider.processName)
                        put("readPermission", provider.readPermission)
                        put("writePermission", provider.writePermission)
                        put("grantUriPermissions", provider.grantUriPermissions)
                        
                        // Authority
                        put("authorities", JSONArray().apply {
                            provider.authority?.split(";")?.forEach { authority ->
                                put(authority)
                            }
                        })
                    })
                }
            })

            // Additional security info
            put("hasSslPinning", checkForSslPinning(packageName))
        }
    }
    
    private fun checkForSslPinning(packageName: String): Boolean {
        try {
            val pm = applicationContext.packageManager
            val appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            
            // Проверяем наличие Network Security Config
            val networkSecurityConfig = appInfo.metaData?.getInt("android.security.net.config", 0) ?: 0
            if (networkSecurityConfig != 0) {
                return true
            }
            
            // Проверяем строки ресурсов на наличие характерных для SSL Pinning подстрок
            val resources = pm.getResourcesForApplication(appInfo)
            val pinningKeywords = listOf(
                "ssl_pin",
                "cert_pin",
                "public_key_pins",
                "certificate_pins",
                "ssl_certificate",
                "pinning_enabled"
            )
            
            resources.assets.list("")?.forEach { asset ->
                if (pinningKeywords.any { keyword -> asset.contains(keyword, ignoreCase = true) }) {
                    return true
                }
            }
            
            // Проверяем наличие характерных классов
            val classLoader = pm.getApplicationInfo(packageName, 0).sourceDir?.let { path ->
                DexFile(path).entries().asSequence().toList()
            } ?: emptyList()
            
            val pinningClassKeywords = listOf(
                "CertificatePinner",
                "SSLPinning",
                "PublicKeyPinner",
                "OkHttpClient",
                "NetworkSecurityConfig"
            )
            
            if (classLoader.any { className ->
                pinningClassKeywords.any { keyword -> className.contains(keyword, ignoreCase = true) }
            }) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
    
    private fun getAllAppsInfo(): JSONObject {
        log("Getting info for all apps")
        val pm = packageManager
        val apps = pm.getInstalledPackages(PackageManager.GET_META_DATA)
        log("Found ${apps.size} installed packages")
        
        return JSONObject().apply {
            val appsArray = JSONArray()
            apps.forEach { packageInfo ->
                log("Processing package: ${packageInfo.packageName}")
                appsArray.put(getAppInfo(packageInfo.packageName))
            }
            put("apps", appsArray)
        }.also { log("Generated all apps info: $it") }
    }
    
    private fun saveAppIcon(packageName: String): String {
        val pm = packageManager
        return try {
            val drawable = pm.getApplicationIcon(packageName)
            val iconFile = File(cacheDir, "icons/${packageName}.png")
            iconFile.parentFile?.mkdirs()
            
            FileOutputStream(iconFile).use { out ->
                drawable.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            iconFile.absolutePath
        } catch (e: Exception) {
            "error: ${e.message}"
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("onStartCommand: ${intent?.extras}")
        
        intent?.let { handleCommand(it) }
        return START_NOT_STICKY
    }
    
    private fun handleCommand(intent: Intent) {
        scope.launch {
            try {
                val type = intent.getIntExtra("type", -1)
                log("Handling command type: $type")
                
                val response = when (type) {
                    MSG_GET_APP_INFO -> {
                        val packageName = intent.getStringExtra("package")
                        log("Getting info for package: $packageName")
                        if (packageName != null) {
                            getAppInfo(packageName)
                        } else {
                            JSONObject().put("error", "No package name provided")
                        }
                    }
                    MSG_GET_ALL_APPS_INFO -> {
                        log("Getting all apps info")
                        getAllAppsInfo()
                    }
                    else -> JSONObject().put("error", "Unknown command type: $type")
                }
                
                // Записываем ответ в файл
                log("Writing response to file: $response")
                withContext(Dispatchers.IO) {
                    Runtime.getRuntime().exec("su").apply {
                        outputStream.write("echo '${response}' > $RESPONSE_FILE\n".toByteArray())
                        outputStream.write("chmod 666 $RESPONSE_FILE\n".toByteArray())
                        outputStream.flush()
                        outputStream.close()
                        waitFor()
                    }
                }
                log("Response written successfully")
            } catch (e: Exception) {
                log("Error handling command: ${e.message}")
                val error = JSONObject().put("error", e.message)
                withContext(Dispatchers.IO) {
                    Runtime.getRuntime().exec("su").apply {
                        outputStream.write("echo '${error}' > $RESPONSE_FILE\n".toByteArray())
                        outputStream.write("chmod 666 $RESPONSE_FILE\n".toByteArray())
                        outputStream.flush()
                        outputStream.close()
                        waitFor()
                    }
                }
            }
        }
    }
} 