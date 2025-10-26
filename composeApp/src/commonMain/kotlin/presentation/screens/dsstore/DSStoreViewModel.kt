package presentation.screens.dsstore

import androidx.compose.runtime.Immutable
import domain.dsstore.Record
import domain.dsstore.DSStoreParser
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import java.io.File

class DSStoreViewModel(private val parser: DSStoreParser, private val httpClient: HttpClient) : ViewModel() {

    private val _uiState = MutableStateFlow(DSStoreUiState())
    val uiState: StateFlow<DSStoreUiState> = _uiState

    fun parseFromUrl(url: String) {
        viewModelScope.launch {
            _uiState.value = DSStoreUiState(isLoading = true)
            try {
                val baseUrl = url.substringBeforeLast("/") + "/"
                val bytes = httpClient.get(url).readBytes()
                val records = parser.parse(bytes)
                println("--- PARSED RECORDS ---")
                records.forEach { record ->
                    println("Filename: ${record.name}")
                    record.fields.forEach { (key, value) ->
                        println("  $key: ${record.humanReadable(key, value)}")
                    }
                }
                println("--------------------")
                _uiState.value = DSStoreUiState(records = records, baseUrl = baseUrl)
            } catch (e: Exception) {
                println("Error parsing from URL: ${e.message}")
                e.printStackTrace()
                _uiState.value = DSStoreUiState(error = e.message)
            }
        }
    }

    fun parseFromFile(path: String) {
        viewModelScope.launch {
            _uiState.value = DSStoreUiState(isLoading = true)
            try {
                val bytes = File(path).readBytes()
                val records = parser.parse(bytes)
                _uiState.value = DSStoreUiState(records = records)
            } catch (e: Exception) {
                _uiState.value = DSStoreUiState(error = e.message)
            }
        }
    }
}

@Immutable
data class DSStoreUiState(
    val records: List<Record> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val baseUrl: String? = null
)
