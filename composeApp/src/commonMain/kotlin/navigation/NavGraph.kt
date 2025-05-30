package navigation

import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.path
import presentation.preset.edit.PresetEditScreen
import presentation.preset.list.PresetListScreen
import presentation.screens.certhash.CertHashScreen
import presentation.screens.settings.SettingsScreen
import presentation.screens.tools.ToolsScreen
import presentation.screens.tomlmerger.TomlMergerScreen
import presentation.screens.rest.RestClientScreen
import presentation.screens.packageManager.PackageManagerScreen

object NavGraph {
    const val TOOLS_ROUTE = "tools"
    const val PRESET_LIST_ROUTE = "preset_list"
    const val PRESET_EDIT_ROUTE = "preset_edit/{id}"
    const val CERT_HASH_ROUTE = "cert_hash"
    const val TOML_MERGER_ROUTE = "toml_merger"
    const val SETTINGS_ROUTE = "settings"
    const val REST_CLIENT_ROUTE = "rest_client"
    const val PACKAGE_MANAGER_ROUTE = "package_manager"
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