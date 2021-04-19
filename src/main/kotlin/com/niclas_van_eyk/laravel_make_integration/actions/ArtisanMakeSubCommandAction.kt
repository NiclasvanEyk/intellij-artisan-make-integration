package com.niclas_van_eyk.laravel_make_integration.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.niclas_van_eyk.laravel_make_integration.LaravelIcons
import com.niclas_van_eyk.laravel_make_integration.filesystem.DirectoryResolver
import com.niclas_van_eyk.laravel_make_integration.laravel.LaravelProject
import com.niclas_van_eyk.laravel_make_integration.resolveLaravelProject
import com.niclas_van_eyk.laravel_make_integration.services.LaravelMakeIntegrationProjectService
import com.niclas_van_eyk.laravel_make_integration.targetFileFromEvent

/**
 * Class for handling the event from the IDE.
 *
 * This mainly validates the prerequisites for executing artisan.
 */
abstract class ArtisanMakeSubCommandAction(
        val command: SubCommand,
        isFromContextMenu: Boolean = false
): DumbAwareAction(LaravelIcons.LaravelLogo) {
    var isFromContextMenu: Boolean = false
        set(value) {
            templatePresentation.text =
                // If this Action is displayed in the context menu, we want to
                // display the short name, as the context is clear
                if (value) command.capitalized
                // Otherwise, the action is displayed out of context, e.g while
                // searching in the double-shift menu. Then we want to provide as
                // much information as possible, so we show the whole description
                // as the text.
                else command.description
            field = value
        }

    init {
        this.isFromContextMenu = isFromContextMenu
        templatePresentation.description = command.description
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val service = project.getService(LaravelMakeIntegrationProjectService::class.java)

        if (!service.isLaravelProject) {
            return
        }

        val targetFilePath = targetFileFromEvent(event)?.canonicalPath
        val laravelProject = service.laravelProject!!

        val execution = buildExecution(
            command, project, laravelProject, targetFilePath
        )

        execution.execute()
    }

    open fun buildExecution(
        meta: SubCommand,
        project: Project,
        laravelProject: LaravelProject,
        targetFilePath: String?
    ): ArtisanMakeSubCommandActionExecution {
        return ArtisanMakeSubCommandActionExecution(
            meta,
            project,
            laravelProject,
            targetFilePath
        )
    }

    override fun update(event: AnActionEvent) {
        super.update(event)

        event.presentation.isEnabledAndVisible = true
        event.presentation.description = command.description

        if (!isFromContextMenu) return

        // We do not want to filter anything from the double shift thingy
        if (event.isFromActionToolbar) return

        val project = resolveLaravelProject(event) ?: return

        val targetFilePath = targetFileFromEvent(event)?.canonicalPath ?: return
        val relativeTargetPath = project.paths.fromProjectRoot(targetFilePath)

        event.presentation.isEnabled = shouldBeActivatedWhenRightClickedOn(relativeTargetPath)
        event.presentation.description = command.description + " (disabled, as this is not the directory where artisan:make would create files of this type)"
    }

    open fun shouldBeActivatedWhenRightClickedOn(relativePathFromProjectRoot: String): Boolean {
        return DirectoryResolver(command.location).couldPointToDefaultFolder(relativePathFromProjectRoot)
    }
}