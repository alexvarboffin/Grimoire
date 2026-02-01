package navigation

import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.path
import presentation.preset.edit.PresetEditScreen
import presentation.preset.list.PresetListScreen
import presentation.screens.certhash.CertHashScreen
import presentation.screens.keystore.KeystoreGeneratorScreen
import presentation.screens.settings.SettingsScreen
import presentation.screens.tools.ToolsScreen
import presentation.screens.tomlmerger.TomlMergerScreen
import presentation.screens.rest.RestClientScreen
import presentation.screens.packageManager.PackageManagerScreen
import presentation.screens.templates.TemplatesScreen
import presentation.screens.templates.TemplateEditScreen
import presentation.screens.batch.BatchGeneratorScreen
import presentation.screens.list.ListGeneratorScreen
import presentation.screens.dsstore.DSStoreScreen
import presentation.screens.list_generator.project_list.ListGeneratorProjectListScreen
import java.net.URLDecoder
import java.net.URLEncoder

object NavGraph {
    const val TOOLS_ROUTE = "tools"
    const val PRESET_LIST_ROUTE = "preset_list"
    const val PRESET_EDIT_ROUTE = "preset_edit/{id}"
    const val CERT_HASH_ROUTE = "cert_hash"
    const val TOML_MERGER_ROUTE = "toml_merger"
    const val SETTINGS_ROUTE = "settings"
    const val REST_CLIENT_ROUTE = "rest_client"
    const val PACKAGE_MANAGER_ROUTE = "package_manager"
    const val KEYSTORE_GENERATOR_ROUTE = "keystore_generator"
    const val TEMPLATES_ROUTE = "templates"
    const val TEMPLATE_EDIT_ROUTE = "template_edit/{filePath}"
    const val BATCH_GENERATOR_ROUTE = "batch_generator"
    const val LIST_GENERATOR_ROUTE = "list_generator"
    const val DS_STORE_PARSER_ROUTE = "ds_store_parser"
    const val LIST_GENERATOR_PROJECT_LIST_ROUTE = "list_generator_project_list"
    const val LIST_GENERATOR_PROJECT_EDIT_ROUTE = "list_generator_project_edit/{id}"
    const val CODEGEN_ROUTE = "codegen"
    const val SIGNER_ROUTE = "signer"
    const val COMMAND_PANEL_ROUTE = "command_panel"
}


fun RouteBuilder.mainGraph(navigator: Navigator) {
    scene(NavGraph.TOOLS_ROUTE) {
        ToolsScreen(
            onNavigateToTool = { route -> 
                navigator.navigate(route)
            },
            onNavigateToSettings = {
                navigator.navigate(NavGraph.SETTINGS_ROUTE)
            }
        )
    }
    
    scene(NavGraph.SETTINGS_ROUTE) {
        SettingsScreen(
            onNavigateBack = { navigator.goBack() }
        )
    }
    
    scene(NavGraph.PRESET_LIST_ROUTE) {
        PresetListScreen(
            onNavigateToEdit = { presetId ->
                val route = if (presetId != null) {
                    "preset_edit/$presetId"
                } else {
                    "preset_edit/new"
                }
                navigator.navigate(route)
            },
            onNavigateBack = { navigator.goBack() }
        )
    }
    
    scene(NavGraph.PRESET_EDIT_ROUTE) { backStackEntry ->
        val presetId = backStackEntry.path<String>("id")?.let { id ->
            if (id == "new") null else id.toLongOrNull()
        }
        PresetEditScreen(
            presetId = presetId,
            onNavigateBack = { navigator.goBack() }
        )
    }

    scene(NavGraph.CERT_HASH_ROUTE) {
        CertHashScreen(
            onNavigateBack = { navigator.goBack() }
        )
    }
  scene(NavGraph.KEYSTORE_GENERATOR_ROUTE) {
        KeystoreGeneratorScreen(
            onNavigateBack = { navigator.goBack() }
        )
    }

    scene(NavGraph.TOML_MERGER_ROUTE) {
        TomlMergerScreen(
            onNavigateBack = { navigator.goBack() }
        )
    }

    scene(NavGraph.REST_CLIENT_ROUTE) {
        RestClientScreen(
            onNavigateBack = { navigator.goBack() }
        )
    }

    scene(NavGraph.PACKAGE_MANAGER_ROUTE) {
        PackageManagerScreen(
            onNavigateBack = { navigator.goBack() }
        )
    }

    scene(NavGraph.TEMPLATES_ROUTE) {
        TemplatesScreen(
            navigator = navigator,
            onNavigateBack = { navigator.goBack() }
        )
    }

    scene(NavGraph.TEMPLATE_EDIT_ROUTE) { backStackEntry ->
        val filePath = backStackEntry.path<String>("filePath")?.let {
            URLDecoder.decode(it, "UTF-8")
        }
        filePath?.let {
            TemplateEditScreen(
                filePath = it,
                onNavigateBack = { navigator.goBack() }
            )
        }
    }

    scene(NavGraph.BATCH_GENERATOR_ROUTE) {
        BatchGeneratorScreen(
            onNavigateBack = { navigator.goBack() }
        )
    }

    scene(NavGraph.LIST_GENERATOR_PROJECT_LIST_ROUTE) {
        ListGeneratorProjectListScreen(
            onNavigateToProject = { projectId ->
                val route = if (projectId != null) {
                    "list_generator_project_edit/$projectId"
                } else {
                    "list_generator_project_edit/new"
                }
                navigator.navigate(route)
            },
            onNavigateBack = { navigator.goBack() }
        )
    }

    scene(NavGraph.LIST_GENERATOR_PROJECT_EDIT_ROUTE) { backStackEntry ->
        val projectId = backStackEntry.path<String>("id")?.let { id ->
            if (id == "new") null else id.toLongOrNull()
        }
        ListGeneratorScreen(
            projectId = projectId,
            onNavigateBack = { navigator.goBack() }
        )
    }

    scene(NavGraph.DS_STORE_PARSER_ROUTE) {
        DSStoreScreen(
            onNavigateBack = { navigator.goBack() }
        )
    }

    scene(NavGraph.CODEGEN_ROUTE) {
        presentation.screens.codegen.CodegenScreen(
            viewModel = org.koin.compose.koinInject(),
            onBack = { navigator.goBack() }
        )
    }

    scene(NavGraph.SIGNER_ROUTE) {
        presentation.screens.signer.SignerScreen(
            viewModel = org.koin.compose.koinInject(),
            onBack = { navigator.goBack() }
        )
    }

    scene(NavGraph.COMMAND_PANEL_ROUTE) {
        presentation.screens.commands.CommandPanelScreen(
            viewModel = org.koin.compose.koinInject(),
            onBack = { navigator.goBack() }
        )
    }
}

fun Navigator.navigateToPresetEdit(presetId: Long? = null) {
    val route = if (presetId != null) {
        NavGraph.PRESET_EDIT_ROUTE.replace("{id}", presetId.toString())
    } else {
        NavGraph.PRESET_EDIT_ROUTE.replace("{id}", "new")
    }
    navigate(route)
}

fun Navigator.navigateBack() {
    goBack()
} 