package com.uzcaptions.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.uzcaptions.app.UzCaptionsApplication
import com.uzcaptions.app.ui.common.exportAndShareSrt
import com.uzcaptions.app.ui.editor.EditorScreen
import com.uzcaptions.app.ui.editor.EditorViewModel
import com.uzcaptions.app.ui.projects.ProjectsScreen
import com.uzcaptions.app.ui.projects.ProjectsViewModel
import com.uzcaptions.app.ui.settings.SettingsScreen
import com.uzcaptions.app.ui.settings.SettingsViewModel
import com.uzcaptions.app.ui.style.StylePickerScreen
import com.uzcaptions.app.ui.style.StylePickerViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun UzCaptionsNavGraph(app: UzCaptionsApplication) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Routes.PROJECTS) {
        composable(Routes.PROJECTS) {
            val vm: ProjectsViewModel = composeViewModel(
                factory = viewModelFactory { initializer { ProjectsViewModel(app.subtitleRepository) } }
            )
            ProjectsScreen(
                viewModel = vm,
                onOpenProject = { id -> navController.navigate(Routes.editor(id)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(
            route = Routes.EDITOR,
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
            val vm: EditorViewModel = composeViewModel(
                factory = viewModelFactory {
                    initializer { EditorViewModel(projectId, app.subtitleRepository, app.sttRepository) }
                }
            )
            EditorScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenStylePicker = { id -> navController.navigate(Routes.stylePicker(id)) },
                onExportSrt = { _, segments ->
                    exportAndShareSrt(context, "subtitr_$projectId", segments)
                }
            )
        }

        composable(
            route = Routes.STYLE_PICKER,
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
            val vm: StylePickerViewModel = composeViewModel(
                factory = viewModelFactory {
                    initializer { StylePickerViewModel(projectId, app.subtitleRepository) }
                }
            )
            StylePickerScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = composeViewModel(
                factory = viewModelFactory { initializer { SettingsViewModel(app.settingsManager) } }
            )
            SettingsScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
    }
}
