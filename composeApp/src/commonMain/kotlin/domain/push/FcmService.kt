package domain.push

interface FcmService {
    suspend fun sendPush(
        serviceAccountJsonPath: String,
        token: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ): Result<String>
}
