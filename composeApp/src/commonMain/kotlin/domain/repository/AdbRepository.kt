package domain.repository

import domain.model.AndroidApp
import domain.model.AndroidDevice
import kotlinx.coroutines.flow.Flow

interface AdbRepository {
    val isRunning: Flow<Boolean>
    suspend fun getDevices(): List<AndroidDevice>
    suspend fun getInstalledApps(deviceId: String): List<AndroidApp>
    suspend fun getAppIcon(deviceId: String, packageName: String): ByteArray?
    suspend fun uninstallApp(deviceId: String, packageName: String)
    suspend fun clearAppData(deviceId: String, packageName: String)
    suspend fun forceStopApp(deviceId: String, packageName: String)
    suspend fun startAdbServer()
    suspend fun stopAdbServer()
    suspend fun openInGooglePlay(deviceId: String, packageName: String)
        suspend fun extractApk(deviceId: String, packageName: String): String
        suspend fun selectDevice(deviceId: String)
        suspend fun executeShellCommand(command: String): String
            fun getLogcat(deviceId: String): Flow<String>
                suspend fun getDeviceInfo(deviceId: String): Map<String, String>
                suspend fun takeScreenshot(deviceId: String): ByteArray
                
                // File System
                suspend fun listFiles(deviceId: String, path: String): List<domain.model.RemoteFile>
                suspend fun pullFile(deviceId: String, remotePath: String, localPath: String)
                suspend fun pushFile(deviceId: String, localPath: String, remotePath: String)
                suspend fun deleteFile(deviceId: String, path: String)
                suspend fun createDirectory(deviceId: String, path: String)
            } 
            