package com.niclas_van_eyk.laravel_make_integration.services.project

import com.google.gson.annotations.SerializedName
import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.niclas_van_eyk.laravel_make_integration.laravel.LaravelApplicationIntrospecter
import com.niclas_van_eyk.laravel_make_integration.laravel.LaravelProject

interface RouteAction
data class ClosureAction(val name: String): RouteAction

interface ClassBasedRouteAction: RouteAction {
    val className: String
}

data class InvocableControllerAction(
    override val className: String
): ClassBasedRouteAction {
    companion object {
        const val INVOKE_METHOD_NAME = "__invoke"
    }
}

data class ControllerMethodAction(
    override val className: String,
    val methodName: String
): ClassBasedRouteAction

/**
 * As returned by `artisan route:list --json`.
 */
data class RouteListEntry(
    @SerializedName("domain") val domain: String,
    @SerializedName("method") val method: String,
    @SerializedName("uri") val uri: String,
    @SerializedName("name") val name: String,
    @SerializedName("action") val rawAction: String,
    @SerializedName("middleware") private val rawMiddleware: String
) {
    val middleware get() = rawMiddleware.split("\n")
    val controllerAction: RouteAction
        get() = if (rawAction.equals("closure", ignoreCase = true)) {
        ClosureAction(rawAction)
    } else {
        val actionParts = rawAction.split("@")
        if (actionParts.size == 1) InvocableControllerAction(actionParts[0])
        else ControllerMethodAction(actionParts[0], actionParts[1])
    }
}

class ProjectRoutes(
    laravelProject: LaravelProject,
    project: Project
) {
    var routes = emptyList<RouteListEntry>()
    private val introspecter = LaravelApplicationIntrospecter(laravelProject, project)

    fun load(onSuccess: (routes: List<RouteListEntry>) -> Unit = {}) {
        introspecter.fetchRouteInfo { routes ->
            this.routes = routes
            onSuccess(routes)
        }
    }
}