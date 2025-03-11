package presentation.screens.certhash

import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import data.certificate.CertificateGrabber
import domain.certificate.CertificateInfo
import domain.certificate.CertificateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

data class CertHashUiState(
    val hostname: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val certificates: List<CertificateInfo> = emptyList(),
    val outputPath: String = "certificate_hashes.txt",
    val showCopiedSnackbar: Boolean = false
)

class CertHashViewModel(
    private val certificateRepository: CertificateRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CertHashUiState())
    val uiState: StateFlow<CertHashUiState> = _uiState.asStateFlow()
    
    private var clipboardManager: ClipboardManager? = null

    fun setClipboardManager(manager: ClipboardManager) {
        clipboardManager = manager
    }

    fun updateHostname(hostname: String) {
        _uiState.value = _uiState.value.copy(hostname = hostname)
    }

    fun updateOutputPath(path: String) {
        _uiState.value = _uiState.value.copy(outputPath = path)
    }

    fun copyToClipboard(text: String) {
        clipboardManager?.setText(AnnotatedString(text))
        _uiState.update { it.copy(showCopiedSnackbar = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(showCopiedSnackbar = false) }
        }
    }

    fun grabCertificates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val certificates = withContext(Dispatchers.IO) {
                    certificateRepository.grabCertificates(
                        hostname = _uiState.value.hostname,
                        outputPath = _uiState.value.outputPath
                    )
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    certificates = certificates
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
} 