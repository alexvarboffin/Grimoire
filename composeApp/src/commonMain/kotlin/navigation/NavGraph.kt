package navigation

import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.path
import presentation.preset.edit.PresetEditScreen
import presentation.preset.list.PresetListScreen

const val PRESET_LIST_ROUTE = "preset_list"
const val PRESET_EDIT_ROUTE = "preset_edit/{id}"

fun RouteBuilder.presetGraph(navigator: Navigator) {
    scene(PRESET_LIST_ROUTE) {
        PresetListScreen(
            onNavigateToEdit = { presetId ->
                val route = if (presetId != null) {
                    "preset_edit/$presetId"
                } else {
                    "preset_edit/new"
                }
                navigator.navigate(route)
            }
        )
    }
    
    scene(PRESET_EDIT_ROUTE) { backStackEntry ->
        val presetId = backStackEntry.path<String>("id")?.let { id ->
            if (id == "new") null else id.toLongOrNull()
        }
        PresetEditScreen(
            presetId = presetId,
            onNavigateBack = { navigator.goBack() }
        )
    }
}

fun Navigator.navigateToPresetEdit(presetId: Long? = null) {
    val route = if (presetId != null) {
        PRESET_EDIT_ROUTE.replace("{id}", presetId.toString())
    } else {
        PRESET_EDIT_ROUTE.replace("?id={id}", "")
    }
    navigate(route)
}

fun Navigator.navigateBack() {
    goBack()
} 