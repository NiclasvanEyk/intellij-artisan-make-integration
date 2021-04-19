package com.niclas_van_eyk.laravel_make_integration.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.niclas_van_eyk.laravel_make_integration.services.LaravelMakeIntegrationProjectService
import com.niclas_van_eyk.laravel_make_integration.toolWindow.commands.CommandsToolWindow
import com.niclas_van_eyk.laravel_make_integration.toolWindow.routes.RoutesToolWindow

class LaravelToolWindowFactory: ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val projectService = project.getService(LaravelMakeIntegrationProjectService::class.java)

        listOf(
            // Until we find a nice way of showing information here,
            // this tab also gets disabled, as the users are likely
            // to care about their routes the most (assumption)
            // Pair("Application", ApplicationToolWindow(toolWindow, project)),
            Pair("Routes", RoutesToolWindow(projectService, project).container),
            Pair("Commands", CommandsToolWindow(projectService).component),
            // Events are not a priority for now
            // Pair("Events", EventsToolWindow(projectService, project).component),
        ).forEach { (tabLabel, content) ->
            toolWindow.contentManager.addContent(
                contentFactory.createContent(content, tabLabel, false)
            )
        }
    }

    override fun isApplicable(project: Project): Boolean {
        return project
                .getService(LaravelMakeIntegrationProjectService::class.java)
                .isLaravelProject
    }
}