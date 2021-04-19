package com.niclas_van_eyk.laravel_make_integration.toolWindow.events

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanelWithEmptyText
import com.niclas_van_eyk.laravel_make_integration.actions.make.MakeEventAction
import com.niclas_van_eyk.laravel_make_integration.actions.make.MakeListenerAction
import com.niclas_van_eyk.laravel_make_integration.services.LaravelMakeIntegrationProjectService
import com.niclas_van_eyk.laravel_make_integration.toolWindow.MasterDetailToolWindow

class EventsToolWindow(
    projectService: LaravelMakeIntegrationProjectService,
    project: Project,
) {
    private val eventList = EventList(
        if (projectService.hasEvents) projectService.events.eventsAndListeners
        else emptyList()
    )

    private val toolbar = ActionToolbarImpl("LaravelToolWindowRoutesTab", DefaultActionGroup().apply {
        add(object : AnAction("Refresh", "Refresh events", AllIcons.Actions.Refresh) {
            override fun actionPerformed(e: AnActionEvent) {
                projectService.events.load {
                    eventList.refreshEvents(it)
                }
            }
        })
        add(object : AnAction("View Options", "View options", AllIcons.Actions.Show) {
            override fun actionPerformed(e: AnActionEvent) {

            }
        })
        addSeparator()
        add(object : AnAction("Create Event", "Create a new event using php artisan make:event", AllIcons.General.Add) {
            override fun actionPerformed(e: AnActionEvent) {
                MakeEventAction().actionPerformed(e)
            }
        })
        add(object : AnAction("Create Listener", "Create a new listener using php artisan make:listener", AllIcons.General.Add) {
            override fun actionPerformed(e: AnActionEvent) {
                MakeListenerAction().actionPerformed(e)
            }
        })
    }, false)

    private val detailView = JBPanelWithEmptyText().withEmptyText("Select an event/listener to see more information")

    val component = MasterDetailToolWindow(eventList, detailView).apply {
        toolbar = this@EventsToolWindow.toolbar
    }
}