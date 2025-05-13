package com.walhalla.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient

actual fun provideHttpClient(): HttpClient {
//    val certificatePinner = CertificatePinner.Builder()
//        .add(pattern = "example.com", pins = listOf("pin1", "pin2")) // TODO: заменить на реальные значения
//        .build()

    val okHttpClient = OkHttpClient.Builder()
        //.proxy(proxy)
        //.certificatePinner(certificatePinner)
        .build()

    return HttpClient(OkHttp) {
//        defaultRequest {
//            header(
//                "User-Agent", "QuickDeal-" + context.packageName + "-" + BuildConfig.VERSION_NAME
//            )
//        }
//            install(ContentNegotiation) {
//                gson()
//            }
//            install(Logging) {
//                logger = Logger.DEFAULT
//                level = LogLevel.HEADERS
////                filter { request ->
////                    println(request.body.toString())
////                    request.url.host.contains("ktor.io")
////                }
//            }
//
//
////            install(HttpTimeout) {
////                requestTimeoutMillis = 1000_00
////            }
//            //install(Logging)
        engine {
            this.preconfigured = okHttpClient
        }
        install(ContentNegotiation) {
//            gson {
//                disableJdkUnsafe() // Отключает использование sun.misc.Unsafe
//            }
            json(Json { prettyPrint = true; isLenient = true;ignoreUnknownKeys = true })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }
//        install(HttpTimeout) {
//            requestTimeoutMillis = 600_000 // Таймаут запроса
//            connectTimeoutMillis = 300_000 // Таймаут подключения
//            socketTimeoutMillis = 300_000 // Таймаут сокета
//        }
    }
}

actual fun platform() = "Desktop" 