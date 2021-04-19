package com.niclas_van_eyk.laravel_make_integration.toolWindow.routes

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.FileColorManager
import com.intellij.ui.ListSpeedSearch
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.niclas_van_eyk.laravel_make_integration.services.project.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.*

fun RouteListEntry.clazz(project: Project): PhpClass? {
    val action = controllerAction

    if (action !is ClassBasedRouteAction) return null

    return PhpIndex.getInstance(project).getClassesByFQN(action.className).first()
}

fun RouteListEntry.jumpToControllerActionSource(project: Project) {
    val clazz = clazz(project) ?: return
    val action = controllerAction
    val methodName = if (action is ControllerMethodAction) action.methodName
                     else InvocableControllerAction.INVOKE_METHOD_NAME
    (clazz.findMethodByName(methodName) ?: clazz).navigate(true)
}

fun RouteListEntry.isVendorRoute(project: Project): Boolean {
    return !isApplicationRoute(project)
}

fun RouteListEntry.isApplicationRoute(project: Project): Boolean {
    val clazz = clazz(project) ?: return false

    clazz.docComment

    // very rudimentary, can surely be improved using clazz.containingFile
    return clazz.fqn.startsWith("\\App\\")
}

fun RouteListEntry.formRequestClass(project: Project): PhpClass? {
    val action = controllerAction

    if (action !is ClassBasedRouteAction) return null

    val clazz = PhpIndex.getInstance(project).getClassesByFQN(action.className).first() ?: return null
    val methodName = if (action is ControllerMethodAction) action.methodName
                     else InvocableControllerAction.INVOKE_METHOD_NAME

    clazz.findMethodByName(methodName)?.parameters?.forEach { parameter ->
        parameter.declaredType.types
            .filter { it.contains("\\") } // quick way to filter out built-in types
            .forEach { type ->
                val realClazz = PhpIndex.getInstance(project).getClassesByFQN(type).firstOrNull()

                realClazz?.extendsList?.referenceElements?.forEach {
                    val fqn = it.declaredType.toString()
                    if (fqn.contains("Illuminate\\Foundation\\Http\\FormRequest"))
                        return realClazz
                }
            }
    }

    return null
}

fun RouteListEntry.canJumpToControllerActionSource() = controllerAction is ClassBasedRouteAction

class RouteList(
    private val routes: List<RouteListEntry>,
    private val project: Project,
    private val onRouteSelected: (RouteListEntry) -> Unit
): JBList<RouteListEntry>(routes) {
    var showMiddlewareParameters = false
        set(value) {
            field = value
            triggerRender()
        }
    var showApplicationRoutes = true
        set(value) {
            field = value
            triggerRender()
        }
    var showVendorRoutes = true
        set(value) {
            field = value
            triggerRender()
        }
    var previouslySelectedRoute: RouteListEntry? = null

    init {
        addMouseListener(object: MouseListener {
            override fun mousePressed(e: MouseEvent?) {
                val point = e?.point ?: return

                selectedIndex = locationToIndex(point)
                val selected = this@RouteList.selectedValue

                if (selected !== previouslySelectedRoute) {
                    onRouteSelected(selected)
                    previouslySelectedRoute = selected
                }

                if (SwingUtilities.isRightMouseButton(e)) {
                    RouteListContextMenu(selected, project).show(this@RouteList, e.x, e.y)
                }

                if (e.clickCount >= 2) {
                    selectedValue.jumpToControllerActionSource(project)
                }
            }
            override fun mouseClicked(e: MouseEvent?) {}
            override fun mouseReleased(e: MouseEvent?) {}
            override fun mouseEntered(e: MouseEvent?) {}
            override fun mouseExited(e: MouseEvent?) {}
        })

        selectionMode = ListSelectionModel.SINGLE_SELECTION

        cellRenderer = object: ColoredListCellRenderer<RouteListEntry>() {
            override fun customizeCellRenderer(list: JList<out RouteListEntry>, value: RouteListEntry?, index: Int, selected: Boolean, hasFocus: Boolean) {
                if (value != null) {
                    icon = iconFor(value.controllerAction)

                    if (value.isVendorRoute(project)) {
                        // It seems that the tree view uses this color to indicate that a file
                        // belongs to a library, so we will do the same
                        background = FileColorManager.getInstance(project).getColor("Yellow")
                    }

                    append(
                        // I personally think its nicer if *all* routes start with a /
                        if (value.uri.startsWith("/")) value.uri else "/${value.uri}",
                        SimpleTextAttributes.REGULAR_ATTRIBUTES,
                        true
                    )

                    append(" ")
                    append(
                        // I do not see the value of HEAD, as it is most likely of no use
                        value.method.replace("GET|HEAD", "GET"),
                        SimpleTextAttributes.GRAYED_ATTRIBUTES,
                        false
                    )

                    append(" ")
                    var middleware = value.middleware

                    // Sometimes the middleware params are only noise, so we hide them by default. Especially since they
                    // are displayed like `web, guest, throttle:1,2,3`, so the added commas are a bit confusing (they
                    // make it harder to differentiate if a new middleware starts or a new parameter).
                    if (!this@RouteList.showMiddlewareParameters) {
                        middleware = middleware.map {
                            if (it.contains(":")) it.substring(0, it.indexOf(":"))
                            else it
                        }
                    }

                    append(
                        middleware.joinToString(", "),
                        SimpleTextAttributes.GRAYED_ATTRIBUTES
                    )
                }
            }
        }

        ListSpeedSearch(this) { it.uri }
    }

    fun refreshRoutes(routes: List<RouteListEntry>) {
        (model as DefaultListModel).apply {
            removeAllElements()
            val shownRoutes = routes
                .filter { showApplicationRoutes || !it.isApplicationRoute(project) }
                .filter { showVendorRoutes || !it.isVendorRoute(project) }
            addAll(shownRoutes)
        }
    }

    fun triggerRender() = this.refreshRoutes(routes)

    fun iconFor(action: RouteAction): Icon = when (action) {
        is ControllerMethodAction -> AllIcons.Nodes.Method
        is InvocableControllerAction -> AllIcons.Nodes.Class
        else /* is Closure */ -> AllIcons.Nodes.Field // As the function icon has the same background color as the class
                                                      // one, we use field here, as it is also just a circled f, but in
                                                      // a different color.
    }
}

class RouteListContextMenu(private val route: RouteListEntry, project: Project): JBPopupMenu() {
    init {
        add(JMenuItem("Jump To Source", AllIcons.Actions.EditSource).apply {
            // Needs to be an invocable controller or a controller method
            isEnabled = route.canJumpToControllerActionSource()
            addActionListener { route.jumpToControllerActionSource(project) }
        })

        val formRequestClass = route.formRequestClass(project)

        if (formRequestClass != null) {
            add(JMenuItem("Jump To Request", AllIcons.Actions.EditSource).apply {
                addActionListener {
                    formRequestClass.navigate(true)
                }
            })
        }
    }
}