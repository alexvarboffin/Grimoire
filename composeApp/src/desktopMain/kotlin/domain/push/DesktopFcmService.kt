package domain.push

import com.google.auth.oauth2.GoogleCredentials
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import java.io.FileInputStream

class DesktopFcmService(private val httpClient: HttpClient, private val json: Json) : FcmService {
    override suspend fun sendPush(
        serviceAccountJsonPath: String,
        token: String,
        title: String,
        body: String,
        data: Map<String, String>
    ): Result<String> {
        return try {
            val credentials = GoogleCredentials.fromStream(FileInputStream(serviceAccountJsonPath))
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            
            credentials.refreshIfExpired()
            val accessToken = credentials.accessToken.tokenValue

            // Get Project ID from JSON
            val serviceAccountJson = json.parseToJsonElement(FileInputStream(serviceAccountJsonPath).bufferedReader().readText()).jsonObject
            val projectId = serviceAccountJson["project_id"]?.jsonPrimitive?.content 
                ?: return Result.failure(Exception("Project ID not found in JSON"))

            val payload = buildJsonObject {
                putJsonObject("message") {
                    put("token", token)
                    putJsonObject("notification") {
                        put("title", title)
                        put("body", body)
                    }
                    if (data.isNotEmpty()) {
                        putJsonObject("data") {
                            data.forEach { (k, v) -> put(k, v) }
                        }
                    }
                }
            }

            val response = httpClient.post("https://fcm.googleapis.com/v1/projects/$projectId/messages:send") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }

            if (response.status.isSuccess()) {
                Result.success(response.bodyAsText())
            } else {
                Result.failure(Exception("FCM Error: ${response.status} - ${response.bodyAsText()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
