package com.uzcaptions.app.ui.navigation

object Routes {
    const val PROJECTS = "projects"
    const val EDITOR = "editor/{projectId}"
    const val STYLE_PICKER = "style/{projectId}"
    const val SETTINGS = "settings"

    fun editor(projectId: Long) = "editor/$projectId"
    fun stylePicker(projectId: Long) = "style/$projectId"
}
