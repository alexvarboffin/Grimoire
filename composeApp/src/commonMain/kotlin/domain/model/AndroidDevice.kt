package domain.model

data class AndroidDevice(
    val id: String,
    val model: String,
    val product: String,
    val isEmulator: Boolean
)

data class AndroidComponent(
    val name: String,
    val type: ComponentType,
    val isExported: Boolean = false,
    val permission: String? = null,
    val enabled: Boolean = true,
    val directBootAware: Boolean = false,
    val process: String? = null,
    val intentFilters: List<IntentFilter> = emptyList()
)

data class IntentFilter(
    val actions: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val dataSchemes: List<String> = emptyList()
)

enum class ComponentType {
    ACTIVITY,
    SERVICE,
    RECEIVER,
    PROVIDER
}

data class AndroidApp(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val isUpdatedSystemApp: Boolean = false,
    val isDebugApp: Boolean = false,
    val isTestOnly: Boolean = false,
    val hasSslPinning: Boolean = false,
    val hasInternetAccess: Boolean = false,
    val allowsBackup: Boolean = true,
    val hasNativeLibs: Boolean = false,
    val isEncryptionAware: Boolean = false,
    val permissions: List<String> = emptyList(),
    val activities: List<AndroidComponent> = emptyList(),
    val services: List<AndroidComponent> = emptyList(),
    val receivers: List<AndroidComponent> = emptyList(),
    val providers: List<AndroidComponent> = emptyList(),
    val installTime: Long,
    val updateTime: Long,
    val targetSdkVersion: Int = 0,
    val minSdkVersion: Int = 0,
    val icon: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        
        other as AndroidApp
        return packageName == other.packageName
    }
    
    override fun hashCode(): Int {
        return packageName.hashCode()
    }
} 