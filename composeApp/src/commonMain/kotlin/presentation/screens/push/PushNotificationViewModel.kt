package presentation.screens.push

import data.local.PushConfigDao
import data.local.PushDeviceDao
import data.local.entities.PushConfigEntity
import data.local.entities.PushDeviceEntity
import domain.push.FcmService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import java.io.File

data class PushListUiState(
    val configs: List<PushConfigEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val toastMessage: String? = null
)

data class PushDetailUiState(
    val config: PushConfigEntity? = null,
    val devices: List<PushDeviceEntity> = emptyList(),
    val pushTitle: String = "Test Push",
    val pushBody: String = "Hello from Grimoire!",
    val pushData: Map<String, String> = emptyMap(),
    val isSending: Boolean = false,
    val lastResponse: String? = null,
    val error: String? = null
)

class PushNotificationViewModel(
    private val configDao: PushConfigDao,
    private val deviceDao: PushDeviceDao,
    private val fcmService: FcmService
) : ViewModel() {

    private val _listState = MutableStateFlow(PushListUiState())
    val listState: StateFlow<PushListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(PushDetailUiState())
    val detailState: StateFlow<PushDetailUiState> = _detailState.asStateFlow()

    init {
        loadConfigs()
    }

    private fun loadConfigs() {
        viewModelScope.launch {
            configDao.getAllConfigs().collect { configs ->
                _listState.update { it.copy(configs = configs) }
            }
        }
    }

    fun loadConfigDetails(configId: Long) {
        viewModelScope.launch {
            val config = configDao.getConfigById(configId)
            _detailState.update { it.copy(config = config) }
            
            deviceDao.getDevicesByConfigId(configId).collect { devices ->
                _detailState.update { it.copy(devices = devices) }
            }
        }
    }

    fun addConfig(name: String, jsonPath: String) {
        viewModelScope.launch {
            configDao.insertConfig(PushConfigEntity(name = name, serviceAccountJsonPath = jsonPath))
        }
    }

    fun deleteConfig(config: PushConfigEntity) {
        viewModelScope.launch {
            configDao.deleteConfig(config)
        }
    }

    fun addDevice(configId: Long, name: String, token: String) {
        viewModelScope.launch {
            deviceDao.insertDevice(PushDeviceEntity(configId = configId, name = name, token = token))
        }
    }

    fun deleteDevice(device: PushDeviceEntity) {
        viewModelScope.launch {
            deviceDao.deleteDevice(device)
        }
    }

    fun updatePushTitle(title: String) {
        _detailState.update { it.copy(pushTitle = title) }
    }

    fun updatePushBody(body: String) {
        _detailState.update { it.copy(pushBody = body) }
    }

    fun addDataField() {
        val currentData = _detailState.value.pushData.toMutableMap()
        currentData["key_${currentData.size + 1}"] = ""
        _detailState.update { it.copy(pushData = currentData) }
    }

    fun removeDataField(key: String) {
        val currentData = _detailState.value.pushData.toMutableMap()
        currentData.remove(key)
        _detailState.update { it.copy(pushData = currentData) }
    }

    fun updateDataField(oldKey: String, newKey: String, value: String) {
        val currentData = _detailState.value.pushData.toMutableMap()
        if (oldKey != newKey) {
            currentData.remove(oldKey)
        }
        currentData[newKey] = value
        _detailState.update { it.copy(pushData = currentData) }
    }

    fun resetDataPayload() {
        _detailState.update { it.copy(pushData = emptyMap()) }
    }

    fun loadExampleData() {
        val exampleData = mapOf(
            "msg.user" to "tester_acc",
            "msg.title" to "Test",
            "msg.body" to "PRIVATEMESSAGE"
        )
        _detailState.update { it.copy(pushData = exampleData) }
    }

    fun clearToastList() {
        _listState.update { it.copy(toastMessage = null) }
    }

    fun exportData(file: File) {
        viewModelScope.launch {
            try {
                val configs = _listState.value.configs
                val exportConfigs = mutableListOf<PushConfigExport>()
                
                for (config in configs) {
                    val devices = deviceDao.getDevicesByConfigId(config.id).first()
                    exportConfigs.add(PushConfigExport(
                        name = config.name,
                        serviceAccountJsonPath = config.serviceAccountJsonPath,
                        pushData = _detailState.value.pushData, 
                        devices = devices.map { PushDeviceExport(it.name, it.token) }
                    ))
                }

                val model = PushExportModel(configs = exportConfigs)
                val json = kotlinx.serialization.json.Json { prettyPrint = true }.encodeToString(PushExportModel.serializer(), model)
                file.writeText(json)
                _listState.update { it.copy(toastMessage = "Exported to ${file.name}") }
            } catch (e: Exception) {
                _listState.update { it.copy(toastMessage = "Export failed: ${e.message}") }
            }
        }
    }

    fun importData(file: File) {
        viewModelScope.launch {
            try {
                val json = file.readText()
                val model = kotlinx.serialization.json.Json.decodeFromString(PushExportModel.serializer(), json)

                for (configExport in model.configs) {
                    val configId = configDao.insertConfig(PushConfigEntity(
                        name = configExport.name,
                        serviceAccountJsonPath = configExport.serviceAccountJsonPath
                    ))
                    for (deviceExport in configExport.devices) {
                        deviceDao.insertDevice(PushDeviceEntity(
                            configId = configId,
                            name = deviceExport.name,
                            token = deviceExport.token
                        ))
                    }
                }
                _listState.update { it.copy(toastMessage = "Imported successfully!") }
            } catch (e: Exception) {
                _listState.update { it.copy(toastMessage = "Import failed: ${e.message}") }
            }
        }
    }

    fun sendPush(device: PushDeviceEntity) {
        val state = _detailState.value
        val config = state.config ?: return
        
        viewModelScope.launch {
            _detailState.update { it.copy(isSending = true, lastResponse = null, error = null) }
            val result = fcmService.sendPush(
                serviceAccountJsonPath = config.serviceAccountJsonPath,
                token = device.token,
                title = state.pushTitle,
                body = state.pushBody,
                data = state.pushData
            )
            
            result.fold(
                onSuccess = { response ->
                    _detailState.update { it.copy(isSending = false, lastResponse = "Success: $response") }
                },
                onFailure = { error ->
                    _detailState.update { it.copy(isSending = false, error = error.message) }
                }
            )
        }
    }

    fun sendPushToAll() {
        val state = _detailState.value
        val config = state.config ?: return
        val devices = state.devices
        if (devices.isEmpty()) return

        viewModelScope.launch {
            _detailState.update { it.copy(isSending = true, lastResponse = null, error = null) }
            var successCount = 0
            var failureCount = 0
            val errors = mutableListOf<String>()

            devices.forEach { device ->
                val result = fcmService.sendPush(
                    serviceAccountJsonPath = config.serviceAccountJsonPath,
                    token = device.token,
                    title = state.pushTitle,
                    body = state.pushBody,
                    data = state.pushData
                )
                result.fold(
                    onSuccess = { successCount++ },
                    onFailure = { error -> 
                        failureCount++
                        errors.add("${device.name}: ${error.message}")
                    }
                )
            }

            _detailState.update { it.copy(
                isSending = false,
                lastResponse = "Broadcast finished. Success: $successCount, Failure: $failureCount",
                error = if (errors.isNotEmpty()) errors.joinToString("") else null
            ) }
        }
    }
}
