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
} 