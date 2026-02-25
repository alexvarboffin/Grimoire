package di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.akuleshov7.ktoml.tree.nodes.TomlNode.Companion.prettyPrint
import com.example.util.JsonToKotlinGenerator
import data.certificate.CertificateGrabber
import data.local.AppDatabase
import data.local.ListGeneratorProjectDao
import data.local.PresetDao
import data.repository.ListGeneratorProjectRepositoryImpl
import data.repository.PresetRepositoryImpl
import data.settings.SettingsDataStore
import domain.certificate.CertificateRepository
import domain.repository.AdbRepository
import domain.repository.PresetRepository
import io.ktor.client.HttpClient
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
import presentation.screens.packageManager.PackageManagerViewModel
import presentation.screens.settings.SettingsViewModel
import presentation.screens.tomlmerger.TomlMergerViewModel
import presentation.screens.rest.RestClientViewModel
import presentation.screens.templates.TemplatesViewModel
import presentation.screens.templates.TemplateEditViewModel
import presentation.screens.batch.BatchGeneratorViewModel
import presentation.screens.list.ListGeneratorViewModel
import presentation.screens.dsstore.DSStoreViewModel
import domain.dsstore.DSStoreParser
import domain.repository.ListGeneratorProjectRepository
import io.ktor.http.ContentType.Application.Json
import kotlinx.serialization.json.Json
import presentation.screens.list_generator.project_list.ListGeneratorProjectListViewModel
import presentation.screens.ossetup.OsSetupViewModel

import theme.ThemeManager
import util.FileProcessor
import java.io.File

val appModule = module {
// Database

    single<AppDatabase> {
        getRoomDatabase(provideDatabase())
    }
    single { provideHttpClient() }
    single { JsonToKotlinGenerator(get()) }
    single { Json { prettyPrint = true; encodeDefaults = true; ignoreUnknownKeys = true } }

    single<PresetDao> { get<AppDatabase>().presetDao() }

    single<ListGeneratorProjectDao> { get<AppDatabase>().listGeneratorProjectDao() }

    single<data.local.CommandPresetDao> { get<AppDatabase>().commandPresetDao() }
    single<data.local.CommandHistoryDao> { get<AppDatabase>().commandHistoryDao() }
    single<data.local.CommandPipelineDao> { get<AppDatabase>().commandPipelineDao() }
    single { get<AppDatabase>().pushConfigDao() }
    single { get<AppDatabase>().pushDeviceDao() }


    // Repository
    //singleOf(::PresetRepositoryImpl) { bind<PresetRepository>() }

    single<PresetRepository> { PresetRepositoryImpl(get()) }

    single<ListGeneratorProjectRepository> { ListGeneratorProjectRepositoryImpl(get()) }


    // DataStore

    single<DataStore<Preferences>> {

        provideDataStore()

    }



    single { SettingsDataStore(get()) }


    // FileSystem

    single { FileSystem.SYSTEM }


    // Utils

    single { FileProcessor(get()) }

    single { ThemeManager(get()) }


    // ViewModels

    single { PresetListViewModel(get(), get()) }

    factory { (presetId: Long?) -> PresetEditViewModel(get(), presetId, get()) }





    single<AdbRepository> { provideAdbRepository() }





    single<CertificateRepository> { CertificateGrabber() }

    single { CertHashViewModel(get()) }

    single { TomlMergerViewModel(get()) }

    single { SettingsViewModel(get()) }

    single { RestClientViewModel(get()) }

    single { PackageManagerViewModel(get()) }

    single { TemplatesViewModel(get()) }

    factory { (filePath: String) -> TemplateEditViewModel(filePath, get()) }

    single { BatchGeneratorViewModel(get()) }

    single { ListGeneratorViewModel(get(), get(), get()) }

    single { DSStoreParser() }

    single { DSStoreViewModel(get(), get()) }

    single { ListGeneratorProjectListViewModel(get()) }

    single { presentation.screens.codegen.CodegenViewModel(get()) }

    single { presentation.screens.signer.SignerViewModel(get()) }

    single { presentation.screens.commands.CommandPanelViewModel(get(), get(), get(), get()) }

    single { presentation.screens.adbviewer.AdbViewerViewModel(get()) }

    single { presentation.screens.fileexplorer.FileExplorerViewModel(get()) }

    single { presentation.screens.push.PushNotificationViewModel(get(), get(), get()) }
    single { OsSetupViewModel() }

}


private fun getRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .addMigrations(data.local.MIGRATION_3_5)
        .addMigrations(data.local.MIGRATION_5_6)
        .fallbackToDestructiveMigration(true)
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

fun provideDatabase(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "my_room_12.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
}

expect fun provideDataStore(): DataStore<Preferences>

expect fun provideHttpClient(): HttpClient

expect fun provideAdbRepository(): AdbRepository
