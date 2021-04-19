package com.niclas_van_eyk.laravel_make_integration.toolWindow.routes

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.niclas_van_eyk.laravel_make_integration.toolWindow.ui.RefreshButtonAction
import com.niclas_van_eyk.laravel_make_integration.actions.make.MakeControllerAction
import com.niclas_van_eyk.laravel_make_integration.toolWindow.ui.FilterActionGroup
import com.niclas_van_eyk.laravel_make_integration.toolWindow.ui.GroupByActionGroup
import com.niclas_van_eyk.laravel_make_integration.toolWindow.ui.ViewOptionsActionGroup

class RoutesToolbar(
    val routeList: RouteList,
    val refreshRoutes: (onFinish: () -> Unit) -> Unit
) {
    val component = ActionToolbarImpl("LaravelToolWindowRoutesTab", DefaultActionGroup().apply {
        add(object : RefreshButtonAction("Refresh", "Refresh routes") {
            override fun actionPerformed(e: AnActionEvent) {
                startRefreshing()
                refreshRoutes { stopRefreshing() }
            }
        })
        add(ViewOptionsActionGroup().apply {
            add(object : ToggleAction("Show Middleware Parameters") {
                override fun isSelected(e: AnActionEvent): Boolean {
                    return routeList.showMiddlewareParameters
                }

                override fun setSelected(e: AnActionEvent, state: Boolean) {
                    routeList.showMiddlewareParameters = state
                }
            })
        })
        add(FilterActionGroup().apply {
            add(object : ToggleAction("Application Routes") {
                override fun isSelected(e: AnActionEvent) = routeList.showApplicationRoutes

                override fun setSelected(e: AnActionEvent, state: Boolean) {
                    routeList.showApplicationRoutes = state
                    // No idea why this is needed here, but if we do not trigger a
                    // rerender here manually, it won't be triggered by the call in
                    // the setter and nothing gets filtered.
                    // So we do it twice in the worst case, which is also fine.
//                    routeList.triggerRender()
                }
            })

            add(object : ToggleAction("Vendor Routes") {
                override fun isSelected(e: AnActionEvent) = routeList.showVendorRoutes

                override fun setSelected(e: AnActionEvent, state: Boolean) {
                    routeList.showVendorRoutes = state
                }
            })
        })
        add(GroupByActionGroup().apply {
            add(object : ToggleAction("Nothing (Flattened)") {
                override fun isSelected(e: AnActionEvent): Boolean {
                    return routeList.showMiddlewareParameters
                }

                override fun setSelected(e: AnActionEvent, state: Boolean) {
                    routeList.showMiddlewareParameters = state
                }
            })
            add(object : ToggleAction("Origin") {
                override fun isSelected(e: AnActionEvent): Boolean {
                    return routeList.showMiddlewareParameters
                }

                override fun setSelected(e: AnActionEvent, state: Boolean) {
                    routeList.showMiddlewareParameters = state
                }
            })
            add(object : ToggleAction("Namespace") {
                override fun isSelected(e: AnActionEvent): Boolean {
                    return routeList.showMiddlewareParameters
                }

                override fun setSelected(e: AnActionEvent, state: Boolean) {
                    routeList.showMiddlewareParameters = state
                }
            })
            add(object : ToggleAction("Name") {
                override fun isSelected(e: AnActionEvent): Boolean {
                    return routeList.showMiddlewareParameters
                }

                override fun setSelected(e: AnActionEvent, state: Boolean) {
                    routeList.showMiddlewareParameters = state
                }
            })
            add(object : ToggleAction("Path") {
                override fun isSelected(e: AnActionEvent): Boolean {
                    return false
                }

                override fun setSelected(e: AnActionEvent, state: Boolean) {
                    routeList.showMiddlewareParameters = state
                }
            })
        })
        addSeparator()
        add(object : AnAction("Create Controller", "Create a new controller using php artisan make:controller", AllIcons.General.Add) {
            override fun actionPerformed(e: AnActionEvent) {
                MakeControllerAction().actionPerformed(e)
            }
        })
    }, false)
}