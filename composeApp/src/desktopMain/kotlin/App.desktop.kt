import Const.DATA_STORE_FILE_NAME
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import java.nio.file.Paths


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