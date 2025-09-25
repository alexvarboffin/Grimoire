package presentation.screens.keystore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

import presentation.components.TopBar



data class Location(val city: String, val state: String, val countryCode: String)

object KeystoreData {
    val names = listOf(
        "Android", "Mobile", "App", "Application", "Release", "Production", "Debug",
        "Store", "PlayStore", "Market", "APK", "Bundle", "AAB", "GooglePlay",
        "Key", "Sign", "Signature", "Cert", "Certificate", "Keystore",
        "Project", "Main", "Core", "Base", "Primary", "Default",
        "Company", "Business", "Enterprise", "Startup", "Studio", "Dev",
        "User", "Client", "Customer", "EndUser", "Final", "Public",
        "Beta", "Alpha", "RC", "Stable", "Nightly", "Canary",
        "Build", "Version", "Variant", "Flavor", "Channel", "Distribution"
    )

    // Или более тематические варианты:
    val androidThemedNames = listOf(
        "App", "AndroidApp", "MobileApp", "Release", "Production", "GooglePlay",
        "StoreRelease", "MarketRelease", "APKSign", "BundleSign", "PlayStore",
        "AndroidCert", "AppCertificate", "SignKey", "ReleaseKey", "Publish",
        "FinalRelease", "PublicRelease", "Stable", "Beta", "Alpha",
        "CompanyApp", "BusinessApp", "EnterpriseApp", "StudioApp", "DevApp",
        "MainApp", "CoreApp", "BaseApp", "PrimaryApp", "DefaultApp",
        "UserApp", "ClientApp", "CustomerApp", "EndUserApp", "PublicApp"
    )

    // Части для составления названий организаций
    private val orgPrefixes = listOf(
        "Alpha", "Beta", "Gamma", "Delta", "Sigma", "Omega", "Quantum", "Stellar", "Nova", "Ultra",
        "Prime", "Elite", "Pro", "Smart", "Fast", "Next", "Future", "Digital", "Tech", "Soft",
        "Global", "World", "International", "National", "City", "Metro", "Urban", "Local"
    )

    private val orgSuffixes = listOf(
        "Solutions", "Systems", "Technologies", "Labs", "Studio", "Works", "Group", "Team", "Dev",
        "Development", "Software", "Apps", "Mobile", "Web", "Cloud", "Data", "AI", "IoT", "Security",
        "Consulting", "Services", "Enterprises", "Ventures", "Partners", "Network", "Digital", "Innovations"
    )

    private val individualSuffixes = listOf(
        "Development", "Software", "Apps", "Solutions", "Studio", "Works", "Lab", "Dev"
    )

    private val personNames = listOf(
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
        "Wilson", "Anderson", "Taylor", "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Moore",
        "Lee", "Clark", "Lewis", "Walker", "Hall", "Allen", "Young", "King", "Wright", "Scott"
    )

    val organizations by lazy {
        val companies = mutableListOf<String>()

        // Крупные компании (префикс + суффикс)
        for (prefix in orgPrefixes) {
            for (suffix in orgSuffixes) {
                companies.add("$prefix $suffix")
            }
        }

        // Компании среднего размера (только префикс или суффикс)
        companies.addAll(orgPrefixes.map { "$it ${orgSuffixes.random()}" })
        companies.addAll(orgSuffixes.map { "${orgPrefixes.random()} $it" })

        // Индивидуальные разработчики
        for (name in personNames) {
            companies.add("$name ${individualSuffixes.random()}")
            companies.add("$name ${orgSuffixes.random()}")
        }

        // Простые варианты для одиночных разработчиков
        val simpleIndividual = listOf(
            "Personal Projects", "Freelance Work", "Independent Developer", "Self Employed",
            "Home Studio", "Personal Studio", "My Apps", "Private Development"
        )
        companies.addAll(simpleIndividual)

        // Случайные комбинации для разнообразия
        repeat(50) {
            val randomPrefix = orgPrefixes.random()
            val randomSuffix = orgSuffixes.random()
            companies.add("$randomPrefix $randomSuffix")
        }

        companies.distinct().shuffled()
    }

    val locations = listOf(
        Location("San Francisco", "California", "US"),
        Location("New York", "New York", "US"),
        Location("London", "London", "GB"),
        Location("Manchester", "Greater Manchester", "GB"),
        Location("Tokyo", "Tokyo", "JP"),
        Location("Paris", "Ile-de-France", "FR"),
        Location("Sydney", "New South Wales", "AU"),
        Location("Berlin", "Berlin", "DE"),
        Location("Toronto", "Ontario", "CA"),
        Location("Mumbai", "Maharashtra", "IN"),
        Location("Shanghai", "Shanghai", "CN")
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeystoreGeneratorScreen(onNavigateBack: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    var generatedCommand by remember { mutableStateOf("") }

    fun generateCommand() {
        val location = KeystoreData.locations.random()
        val organization = KeystoreData.organizations.random()
        val name = KeystoreData.names.random()

        val dname = "CN=$name, OU=$organization, O=$organization, L=${location.city}, ST=${location.state}, C=${location.countryCode}"

        generatedCommand = "keytool -genkey -v -keystore keystore.jks -alias release -keyalg RSA -keysize 2048 -validity 10000 -dname \"$dname\" -storepass release -keypass release"
    }

    LaunchedEffect(Unit) {
        generateCommand()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keystore Generator") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Сгенерированная команда для создания keystore.jks:",
                style = MaterialTheme.typography.titleMedium
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                SelectionContainer {
                    Text(
                        text = generatedCommand,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { generateCommand() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Сгенерировать новую")
                }
                Button(
                    onClick = { clipboardManager.setText(AnnotatedString(generatedCommand)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Копировать")
                }
            }

            // Показать примеры организаций для демонстрации
            Text(
                text = "Примеры генерируемых организаций:",
                style = MaterialTheme.typography.titleSmall
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(10) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = KeystoreData.organizations.random(),
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}