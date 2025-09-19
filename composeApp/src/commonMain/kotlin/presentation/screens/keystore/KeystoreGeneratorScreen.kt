package presentation.screens.keystore

import androidx.compose.foundation.layout.*
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
import domain.model.KeystoreData

import presentation.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeystoreGeneratorScreen(onNavigateBack: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    var generatedCommand: String by remember { mutableStateOf("") }

    fun generateCommand() {
        val country = KeystoreData.countries.entries.random()
        val organization = KeystoreData.organizations.random()
        val name = KeystoreData.names.random()
        val city = KeystoreData.cities.random()
        val state = KeystoreData.states.random()

        val dname = "CN=$name, OU=$organization, O=$organization, L=$city, ST=$state, C=${country.key}"
        
        generatedCommand = "keytool -genkey -v -keystore keystore.jks -alias release -keyalg RSA -keysize 2048 -validity 10000 -dname \"$dname\" -storepass release -keypass release"
    }

    LaunchedEffect(Unit) {
        generateCommand()
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Keystore Generator",

            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
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
                    onClick = { clipboardManager?.setText(AnnotatedString(generatedCommand)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Копировать")
                }
            }
        }
    }
}
