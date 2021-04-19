package com.niclas_van_eyk.laravel_make_integration.toolWindow.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.DefaultActionGroup

class ViewOptionsActionGroup(): DefaultActionGroup() {
    init {
        templatePresentation.apply {
            text = "View Options"
            icon = AllIcons.Actions.Show
        }
        isPopup = true
        addSeparator("View Options")
    }
}