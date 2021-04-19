package com.niclas_van_eyk.laravel_make_integration.toolWindow.commands

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBPanelWithEmptyText
import com.niclas_van_eyk.laravel_make_integration.toolWindow.ui.RefreshButtonAction
import com.niclas_van_eyk.laravel_make_integration.actions.make.MakeCommandAction
import com.niclas_van_eyk.laravel_make_integration.services.LaravelMakeIntegrationProjectService
import com.niclas_van_eyk.laravel_make_integration.toolWindow.MasterDetailToolWindow

class CommandsToolWindow(
    projectService: LaravelMakeIntegrationProjectService
): SimpleToolWindowPanel(false) {
    private val commandList = CommandList(
        if (projectService.hasCommands) projectService.commands.commands
        else emptyList()
    )

    private val detailView = JBPanelWithEmptyText().withEmptyText("Select a command to see more information")

    private val toolbar = ActionToolbarImpl("LaravelToolWindowCommandsTab", DefaultActionGroup().apply {
        add(object : RefreshButtonAction("Refresh", "Refresh commands") {
            override fun actionPerformed(e: AnActionEvent) {
                startRefreshing()
                projectService.commands.load {
                    // TODO: Handle error case
                    commandList.refreshCommands(it)
                    stopRefreshing()
                }
            }
        })
        add(object : AnAction("View Options", "View options", AllIcons.Actions.Show) {
            override fun actionPerformed(e: AnActionEvent) {
//                routeList.showMiddlewareParameters = !routeList.showMiddlewareParameters
            }
        })
        addSeparator()
        add(object : AnAction("Create Command", "Create a new command using php artisan make:command", AllIcons.General.Add) {
            override fun actionPerformed(e: AnActionEvent) {
                MakeCommandAction().actionPerformed(e)
            }
        })
    }, false)

    val component = MasterDetailToolWindow(commandList, detailView).apply {
        toolbar = this@CommandsToolWindow.toolbar
    }
}

