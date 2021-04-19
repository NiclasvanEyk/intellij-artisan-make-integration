package com.niclas_van_eyk.laravel_make_integration.toolWindow.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

abstract class RefreshButtonAction(
    val text: String,
    val description: String,
): AnAction(text, description, AllIcons.Actions.Refresh) {
    var isRefreshing = false

    protected fun startRefreshing() {
        isRefreshing = true
    }

    protected fun stopRefreshing() {
        isRefreshing = false
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isEnabled = !isRefreshing
    }
}