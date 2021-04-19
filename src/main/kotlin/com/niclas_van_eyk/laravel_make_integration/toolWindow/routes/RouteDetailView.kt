package com.niclas_van_eyk.laravel_make_integration.toolWindow.routes

import com.intellij.codeInsight.documentation.DocumentationComponent
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanelWithEmptyText
import com.jetbrains.php.PhpIndex
import com.niclas_van_eyk.laravel_make_integration.services.project.ClassBasedRouteAction
import com.niclas_van_eyk.laravel_make_integration.services.project.ControllerMethodAction
import com.niclas_van_eyk.laravel_make_integration.services.project.InvocableControllerAction
import com.niclas_van_eyk.laravel_make_integration.services.project.RouteAction
import javax.swing.JComponent

class RouteDetailView(
    private val project: Project,
    private val setDetailComponent: (component: JComponent) -> Unit
) {
    companion object {
        val emptyView = JBPanelWithEmptyText().withEmptyText("Select a route to see more information")
    }

    var selectedRoute: RouteAction? = null
        set(value) {
            field = value
            updateDocumentationPanel()
        }

    init {
        clearDetailComponent()
    }

    private fun updateDocumentationPanel() {
        clearDetailComponent()

        val action = selectedRoute
        if (action !is ClassBasedRouteAction) return
        val clazz = PhpIndex.getInstance(project).getClassesByFQN(action.className).firstOrNull() ?: return
        val method = if (action is ControllerMethodAction) action.methodName
                     else InvocableControllerAction.INVOKE_METHOD_NAME
        val documentedCodePart = clazz.findMethodByName(method) ?: clazz

        val documentationViewer = DocumentationComponent.createAndFetch(project, documentedCodePart) {}
        setDetailComponent(documentationViewer)
    }

    private fun clearDetailComponent() {
        setDetailComponent(emptyView)
    }
}