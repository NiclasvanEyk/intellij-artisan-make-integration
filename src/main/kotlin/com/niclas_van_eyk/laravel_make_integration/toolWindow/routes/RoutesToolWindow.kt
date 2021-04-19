package com.niclas_van_eyk.laravel_make_integration.toolWindow.routes

import com.intellij.openapi.project.Project
import com.niclas_van_eyk.laravel_make_integration.services.LaravelMakeIntegrationProjectService
import com.niclas_van_eyk.laravel_make_integration.toolWindow.MasterDetailToolWindow

class RoutesToolWindow(
    projectService: LaravelMakeIntegrationProjectService,
    project: Project,
) {
    private val routeList: RouteList = RouteList(
        if (projectService.hasRoutes) projectService.routes.routes
        else emptyList(),
        project
    ) {
        detailView.selectedRoute = it.controllerAction
    }

    // TODO: look into how the TODO-ToolWindow displays
    //       the comments inline + how they do the spacing
    private val toolbar: RoutesToolbar = RoutesToolbar(
        routeList
    ) { onFinish: () -> Unit ->
        projectService.routes.load {
            routeList.refreshRoutes(it)
            // TODO: Also call this in the error case
            onFinish()
        }
    }

    val container: MasterDetailToolWindow = MasterDetailToolWindow(routeList, RouteDetailView.emptyView).apply {
        toolbar = this@RoutesToolWindow.toolbar.component
    }

    private val detailView: RouteDetailView = RouteDetailView(project) { newDetail -> container.component.secondComponent = newDetail }
}