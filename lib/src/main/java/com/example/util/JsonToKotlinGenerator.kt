package com.example.util

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull


fun main() {
    var url = "https://jsonplaceholder.typicode.com/users"

    //JsonToKotlinGenerator().generateDataClassFromUrl(url = url, className = "User")

}
//fun provideHttpClient(): HttpClient {
//
//    return HttpClient(OkHttp) {
//
//        val okHttpClient = OkHttpClient.Builder()
//            //.proxy(proxy)
//            .build()
//
////        defaultRequest {
////            header(
////                "User-Agent", "QuickDeal-" + context.packageName + "-" + BuildConfig.VERSION_NAME
////            )
////        }
//
////            install(ContentNegotiation) {
////                gson()
////            }
////            install(Logging) {
////                logger = Logger.DEFAULT
////                level = LogLevel.HEADERS
//////                filter { request ->
//////
//////                    println(request.body.toString())
//////                    request.url.host.contains("ktor.io")
//////                }
////            }
////
////
//////            install(HttpTimeout) {
//////                requestTimeoutMillis = 1000_00
//////            }
////            //install(Logging)
//
////        engine {
////            this.preconfigured = okHttpClient
////        }
//        install(ContentNegotiation) {
////            gson {
////                disableJdkUnsafe() // Отключает использование sun.misc.Unsafe
////            }
//            json(Json { prettyPrint = true; isLenient = true;ignoreUnknownKeys = true })
//        }
//
//        install(Logging) {
//            logger = Logger.DEFAULT
//            level = LogLevel.HEADERS
//        }
////        install(HttpTimeout) {
////            requestTimeoutMillis = 600_000 // Таймаут запроса
////            connectTimeoutMillis = 300_000 // Таймаут подключения
////            socketTimeoutMillis = 300_000 // Таймаут сокета
////        }
//    }
//
//    //CIO Java
////    return HttpClient(OkHttp) {
////////        install(JsonFeature) {
////////            serializer = KotlinxSerializer()
////////        }
//////        install(ContentNegotiation) {
//////            gson()
//////        }
//////        install(Logging) {
//////            level = LogLevel.INFO
//////        }
//////        install(Logging) {
//////            logger = Logger.DEFAULT
//////            level = LogLevel.HEADERS
//////        }
//////        install(HttpTimeout) {
//////            requestTimeoutMillis = 600_000 // Таймаут запроса
//////            connectTimeoutMillis = 300_000 // Таймаут подключения
//////            socketTimeoutMillis = 300_000 // Таймаут сокета
//////        }
////    }
//}
class JsonToKotlinGenerator(var httpClient: HttpClient) {

    fun generateDataClassFromUrl(url: String, className: String) {
//        httpClient.get(url)
//        generateDataClass()
    }

    fun generateDataClass(json: String, className: String = "Response"): String {
        return try {
            val jsonElement = Json.Default.parseToJsonElement(json)
            buildString {
                appendLine("import kotlinx.serialization.SerialName")
                appendLine("import kotlinx.serialization.Serializable")
                appendLine()
                when (jsonElement) {
                    is JsonArray -> {
                        val itemElement = jsonElement.firstOrNull()
                        if (itemElement is JsonObject) {
                            append(generateClass(itemElement, className))
                        } else {
                            append("// Ошибка: Массив не содержит объектов")
                        }
                    }

                    is JsonObject -> append(generateClass(jsonElement, className))
                    else -> append("// Ошибка: Ожидается объект или массив объектов")
                }
            }
        } catch (e: Exception) {
            "// Ошибка парсинга JSON: ${e.message}"
        }
    }

    fun generateRetrofitInterface(
        baseUrl: String,
        path: String,
        method: String,
        className: String
    ): String {
        return buildString {
            appendLine("interface ${className}Api {")
            appendLine("    @${method.uppercase()}")
            appendLine("    @Url(\"$path\")")
            appendLine("    suspend fun get${className}(): ${className}Response")
            appendLine("}")
        }
    }

    fun generateKtorClient(
        baseUrl: String,
        path: String,
        method: String,
        className: String
    ): String {
        return buildString {
            appendLine("class ${className}Client(private val client: HttpClient) {")
            appendLine("    suspend fun get${className}(): ${className}Response {")
            appendLine("        return client.request(\"$baseUrl$path\") {")
            appendLine("            method = HttpMethod.${method.uppercase()}")
            appendLine("        }.body()")
            appendLine("    }")
            appendLine("}")
        }
    }

    fun generateRepository(className: String): String {
        return buildString {
            appendLine("interface ${className}Repository {")
            appendLine("    suspend fun get${className}(): ${className}Response")
            appendLine("}")
            appendLine()
            appendLine("class ${className}RepositoryImpl(")
            appendLine("    private val client: ${className}Client")
            appendLine(") : ${className}Repository {")
            appendLine("    override suspend fun get${className}(): ${className}Response {")
            appendLine("        return client.get${className}()")
            appendLine("    }")
            appendLine("}")
        }
    }

    fun generateKtorClientResult(
        baseUrl: String,
        path: String,
        method: String,
        className: String
    ): String {
        return buildString {
            appendLine("class ${className}Client(private val client: HttpClient) {")
            appendLine("    companion object {")
            appendLine("        private const val _DEBUG_ = true")
            appendLine("    }")
            appendLine()
            appendLine("    suspend fun get$className(): Result<${className}Response> {")
            appendLine("        try {")
            appendLine("            val response: HttpResponse = client.${method.lowercase()}(\"$baseUrl$path\")")
            appendLine("            println(\"${className}: \" + response.bodyAsText())")
            appendLine()
            appendLine("            return if (response.status.isSuccess()) {")
            appendLine("                try {")
            appendLine("                    Result.success(response.body())")
            appendLine("                } catch (e: Exception) {")
            appendLine("                    e.printStackTrace()")
            appendLine("                    Result.failure(e)")
            appendLine("                }")
            appendLine("            } else {")
            appendLine("                try {")
            appendLine("                    val errorResponse: ErrorResponse = response.body()")
            appendLine("                    val msg = errorResponse.message")
            appendLine()
            appendLine("                    if (_DEBUG_) {")
            appendLine("                        DLog.d(\"ОШИБКА СЕРВЕРА: \" + errorResponse.toString())")
            appendLine("                        DLog.d(\"ОШИБКА СЕРВЕРА: \" + errorResponse.message)")
            appendLine("                    }")
            appendLine()
            appendLine("                    Result.failure(Exception(msg))")
            appendLine("                } catch (e: Exception) {")
            appendLine("                    Result.failure(Exception(\"Unknown error occurred, status: \${response.status}, body: \${response.bodyAsText()}\"))")
            appendLine("                }")
            appendLine("            }")
            appendLine("        } catch (e: HttpRequestTimeoutException) {")
            appendLine("            DLog.d(\"Превышено время ожидания запроса: \${e.message}\")")
            appendLine("            return Result.failure(Exception(\"Превышено время ожидания запроса. Попробуйте позже\"))")
            appendLine("        } catch (e: Throwable) {")
            appendLine("            return handleException(e)")
            appendLine("        }")
            appendLine("    }")
            appendLine()
            appendLine("    private fun handleException(e: Throwable): Result<${className}Response> {")
            appendLine("        return when(e) {")
            appendLine("            is IOException -> Result.failure(Exception(\"Проверьте подключение к интернету\"))")
            appendLine("            is HttpRequestTimeoutException -> Result.failure(Exception(\"Превышено время ожидания запроса\"))")
            appendLine("            is SerializationException -> Result.failure(Exception(\"Ошибка обработки данных\"))")
            appendLine("            else -> Result.failure(Exception(e.message ?: \"Неизвестная ошибка\"))")
            appendLine("        }")
            appendLine("    }")
            appendLine("}")
            appendLine()
            appendLine("@Serializable")
            appendLine("data class ErrorResponse(")
            appendLine("    val statusCode: Int? = null,")
            appendLine("    val message: String = \"Unknown error\",")
            appendLine("    val error: String? = null")
            appendLine(")")
        }
    }

    private fun generateClass(jsonObject: JsonObject, className: String, level: Int = 0): String {
        val indent = "    ".repeat(level)
        val builder = StringBuilder()

        // Добавляем аннотацию Serializable только для корневого класса
        if (level == 0) {
            builder.appendLine("@Serializable")
        }
        builder.appendLine("${indent}data class $className(")

        jsonObject.entries.forEachIndexed { index, (key, value) ->
            val isLast = index == jsonObject.entries.size - 1
            val formattedKey = formatPropertyName(key)
            val originalValue =
                value.toString().take(50) // Берем первые 50 символов для комментария

            // Добавляем комментарий с примером значения
            builder.appendLine("${indent}    // Example: $originalValue${if (originalValue.length >= 50) "..." else ""}")

            // Добавляем SerialName если имя поля было изменено
            if (formattedKey != key) {
                builder.appendLine("${indent}    @SerialName(\"$key\")")
            }

            when (value) {
                is JsonObject -> {
                    // Определяем имя класса на основе contentType или ключа
                    val nestedClassName = value.getNestedClassName(key)
                    builder.appendLine()
                    builder.append(generateClass(value, nestedClassName, level + 1))
                    builder.appendLine()
                    builder.append("${indent}    val $formattedKey: $nestedClassName? = null${if (!isLast) "," else ""}")
                }

                is JsonArray -> {
                    val itemElement = value.firstOrNull()
                    when (itemElement) {
                        is JsonObject -> {
                            val itemClassName = itemElement.getNestedClassName(
                                key.removeSuffix("s").capitalize() + "Item"
                            )
                            builder.appendLine()
                            builder.append(generateClass(itemElement, itemClassName, level + 1))
                            builder.appendLine()
                            builder.append("${indent}    val $formattedKey: List<$itemClassName> = emptyList()${if (!isLast) "," else ""}")
                        }

                        is JsonPrimitive -> {
                            val itemType = getTypeForPrimitive(itemElement)
                            builder.append("${indent}    val $formattedKey: List<$itemType> = emptyList()${if (!isLast) "," else ""}")
                        }

                        else -> builder.append("${indent}    val $formattedKey: List<String> = emptyList()${if (!isLast) "," else ""}")
                    }
                }

                is JsonPrimitive -> {
                    val type = getTypeForPrimitive(value)
                    if (key == "contentType") {
                        builder.append("${indent}    val $formattedKey: $type = \"\"${if (!isLast) "," else ""}")
                    } else {
                        builder.append("${indent}    val $formattedKey: $type? = null${if (!isLast) "," else ""}")
                    }
                }

                else -> {
                    if (key == "contentType") {
                        builder.append("${indent}    val $formattedKey: String = \"\"${if (!isLast) "," else ""}")
                    } else {
                        builder.append("${indent}    val $formattedKey: String? = null${if (!isLast) "," else ""}")
                    }
                }
            }
            builder.appendLine()
        }

        builder.append("${indent})")
        return builder.toString()
    }

    private fun JsonObject.getNestedClassName(defaultName: String): String {
        // Пытаемся получить имя класса из contentType
        return this["contentType"]?.jsonPrimitive?.contentOrNull?.let { type ->
            type.split(".").last().capitalize()
        } ?: defaultName.capitalize()
    }

    private fun formatPropertyName(name: String): String {
        // Преобразуем имя в camelCase
        return name.split(Regex("[^a-zA-Z0-9]"))
            .mapIndexed { index, part ->
                if (index == 0) part.lowercase()
                else part.capitalize()
            }
            .joinToString("")
            .let {
                if (it.first().isDigit()) "_$it" else it
            }
    }

    private fun getTypeForPrimitive(primitive: JsonPrimitive): String {
        return when {
            primitive.isString -> "String"
            primitive.intOrNull != null -> "Int"
            primitive.longOrNull != null -> "Long"
            primitive.doubleOrNull != null -> "Double"
            primitive.booleanOrNull != null -> "Boolean"
            else -> "String"
        }
    }

    private fun getDefaultValue(type: String): String {
        return when (type) {
            "String" -> "\"\""
            "Double" -> "-1.0"
            "Long" -> "-1L"
            "Int" -> "-1"
            "Boolean" -> "false"
            else -> "null"
        }
    }

    private fun String.capitalize() =
        replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

}