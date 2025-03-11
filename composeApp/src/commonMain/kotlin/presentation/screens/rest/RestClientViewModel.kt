package presentation.screens.rest

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

data class ResponseData(
    val statusCode: Int = 0,
    val statusText: String = "",
    val body: String = "",
    val headers: Map<String, List<String>> = emptyMap(),
    val cookies: Map<String, String> = emptyMap()
)

data class RestClientState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val response: ResponseData? = null
)

class RestClientViewModel(
    private val client: HttpClient
) : ViewModel() {
    private val _state = MutableStateFlow(RestClientState())
    val state: StateFlow<RestClientState> = _state.asStateFlow()

    fun sendRequest(
        url: String,
        method: String,
        headers: String,
        body: String
    ) {
        if (url.isBlank()) {
            _state.value = _state.value.copy(error = "URL не может быть пустым")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val response = client.request(url) {
                    this.method = HttpMethod.parse(method)

                    // Parse and add headers
                    headers.lines()
                        .filter { it.isNotBlank() }
                        .forEach { line ->
                            val parts = line.split(":", limit = 2)
                            if (parts.size == 2) {
                                header(parts[0].trim(), parts[1].trim())
                            }
                        }

                    // Add body for appropriate methods
                    if (method !in listOf("GET", "HEAD", "OPTIONS") && body.isNotBlank()) {
                        setBody(body)
                    }
                }

                val responseText = response.bodyAsText()
                
                // Обработка заголовков
                val responseHeaders = mutableMapOf<String, List<String>>()
                response.headers.forEach { key, values ->
                    responseHeaders[key] = values
                }
                
                // Обработка cookies
                val cookies = responseHeaders["Set-Cookie"]?.mapNotNull { cookieStr ->
                    val parts = cookieStr.split(";")[0].split("=", limit = 2)
                    if (parts.size == 2) parts[0] to parts[1] else null
                }?.toMap() ?: emptyMap()

                _state.value = _state.value.copy(
                    isLoading = false,
                    response = ResponseData(
                        statusCode = response.status.value,
                        statusText = response.status.description,
                        body = responseText,
                        headers = responseHeaders,
                        cookies = cookies
                    )
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Ошибка: ${e.message}"
                )
            }
        }
    }

    override fun onCleared() {
        client.close()
        super.onCleared()
    }
} 