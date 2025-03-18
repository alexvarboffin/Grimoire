package di


import Const.DATA_STORE_FILE_NAME
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import data.adb.AdbRepositoryImpl
import domain.repository.AdbRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okio.Path.Companion.toPath
import java.nio.file.Paths

actual fun provideHttpClient(): HttpClient {

    return HttpClient(OkHttp) {

        val okHttpClient = OkHttpClient.Builder()
            //.proxy(proxy)
            .build()

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
////
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

//        engine {
//            this.preconfigured = okHttpClient
//        }
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

    //CIO Java
//    return HttpClient(OkHttp) {
//////        install(JsonFeature) {
//////            serializer = KotlinxSerializer()
//////        }
////        install(ContentNegotiation) {
////            gson()
////        }
////        install(Logging) {
////            level = LogLevel.INFO
////        }
////        install(Logging) {
////            logger = Logger.DEFAULT
////            level = LogLevel.HEADERS
////        }
////        install(HttpTimeout) {
////            requestTimeoutMillis = 600_000 // Таймаут запроса
////            connectTimeoutMillis = 300_000 // Таймаут подключения
////            socketTimeoutMillis = 300_000 // Таймаут сокета
////        }
//    }
}

actual fun provideAdbRepository(): AdbRepository  = AdbRepositoryImpl()


object DataStoreProvider {
    private var instance: DataStore<Preferences>? = null

    fun getInstance(): DataStore<Preferences> {
        return instance ?: synchronized(this) {
            instance ?: createDataStore(producePath = {
                val path = Paths.get(System.getProperty("user.home"), DATA_STORE_FILE_NAME)
                path.toString()
            }).also { instance = it }
        }
    }

    private fun createDataStore(producePath: () -> String): DataStore<Preferences> {
        return PreferenceDataStoreFactory.createWithPath(
            produceFile = { producePath().toPath() }
        )
    }
}

actual fun provideDataStore(): DataStore<Preferences> = DataStoreProvider.getInstance()