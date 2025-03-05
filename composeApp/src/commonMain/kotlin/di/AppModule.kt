package di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import data.certificate.CertificateGrabber
import data.local.AppDatabase
import data.local.PresetDao
import data.repository.PresetRepositoryImpl
import data.settings.SettingsDataStore
import domain.certificate.CertificateRepository
import domain.repository.PresetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import moe.tlaster.precompose.viewmodel.viewModel
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import presentation.preset.edit.PresetEditViewModel
import presentation.preset.list.PresetListViewModel
import presentation.screens.certhash.CertHashScreen
import presentation.screens.certhash.CertHashViewModel
import presentation.screens.settings.SettingsViewModel
import presentation.screens.tomlmerger.TomlMergerViewModel
import theme.ThemeManager
import util.FileProcessor
import java.io.File

val appModule = module {
    // Database

    single<AppDatabase> {
        getRoomDatabase(provideDatabase())
    }

    single<PresetDao> { get<AppDatabase>().presetDao() }

    // Repository
    //singleOf(::PresetRepositoryImpl) { bind<PresetRepository>() }
    single<PresetRepository> { PresetRepositoryImpl(get()) }



    single { SettingsDataStore(get()) }

    // FileSystem
    single { FileSystem.SYSTEM }

    // Utils
    single { FileProcessor(get()) }
    single { ThemeManager(get()) }

    // ViewModels
    single { PresetListViewModel(get(), get()) }
    factory { (presetId: Long?) -> PresetEditViewModel(get(), presetId, get()) }

    single<CertificateRepository> { CertificateGrabber() }
    single { CertHashViewModel(get()) }
    single { TomlMergerViewModel(get()) }
    single { SettingsViewModel(get()) }
}

fun getRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        //.addMigrations(MIGRATIONS)
        //.fallbackToDestructiveMigrationOnDowngrade()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

fun provideDatabase(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "my_room.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
}