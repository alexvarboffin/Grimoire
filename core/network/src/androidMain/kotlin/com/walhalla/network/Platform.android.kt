package com.walhalla.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

fun OkHttpClient.Builder.ignoreAllSSLErrors(): OkHttpClient.Builder {
    val naiveTrustManager = object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) = Unit
        override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) = Unit
    }

    val insecureSocketFactory = SSLContext.getInstance("TLSv1.2").apply {
        val trustAllCerts = arrayOf<TrustManager>(naiveTrustManager)
        init(null, trustAllCerts, SecureRandom())
    }.socketFactory

    sslSocketFactory(insecureSocketFactory, naiveTrustManager)
    hostnameVerifier(HostnameVerifier { _, _ -> true })
    return this
}

actual fun provideHttpClient(): HttpClient {
//    return HttpClient(OkHttp) {
//        install(ContentNegotiation) {
//            gson()
//        }
//        install(Logging) {
//            level = LogLevel.BODY
//        }
//    }
//    val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("10.0.20.167", 8888))

//    val certificatePinner = CertificatePinner.Builder()
//        .add(pattern = String(Const.PINNED_HOST_NAME), pins = Const.pins)
//        .build()
    
    val okHttpClient = OkHttpClient.Builder()
        //.proxy(proxy)
        //.certificatePinner(certificatePinner)
        .apply {
            ignoreAllSSLErrors()
        }.build()

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
            preconfigured = okHttpClient
        }
        install(ContentNegotiation) {
            //gson()
            json(Json { prettyPrint = true; isLenient = true;ignoreUnknownKeys = true })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 600_000 // Таймаут запроса
            connectTimeoutMillis = 300_000 // Таймаут подключения
            socketTimeoutMillis = 300_000 // Таймаут сокета
        }
    }
}

actual fun platform() = "Android"