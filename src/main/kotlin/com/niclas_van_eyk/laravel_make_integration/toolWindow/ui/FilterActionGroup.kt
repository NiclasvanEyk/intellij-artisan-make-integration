package com.niclas_van_eyk.laravel_make_integration.toolWindow.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.DefaultActionGroup

class FilterActionGroup(): DefaultActionGroup() {
    init {
        templatePresentation.apply {
            text = "Filter"
            icon = AllIcons.General.Filter
        }
        isPopup = true
        addSeparator("Filter")
    }
}