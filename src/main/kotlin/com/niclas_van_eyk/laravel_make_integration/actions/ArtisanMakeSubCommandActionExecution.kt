package com.niclas_van_eyk.laravel_make_integration.actions

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.jetbrains.php.run.PhpEditInterpreterExecutionException
import com.niclas_van_eyk.laravel_make_integration.filesystem.CreatedFileResolver
import com.niclas_van_eyk.laravel_make_integration.filesystem.DirectoryResolver
import com.niclas_van_eyk.laravel_make_integration.ide.IdeAdapter
import com.niclas_van_eyk.laravel_make_integration.ide.jetbrains.JetbrainsAdapter
import com.niclas_van_eyk.laravel_make_integration.ide.jetbrains.PluginNotifications
import com.niclas_van_eyk.laravel_make_integration.laravel.ArtisanMakeParameters
import com.niclas_van_eyk.laravel_make_integration.laravel.LaravelProject
import com.niclas_van_eyk.laravel_make_integration.run.NoInterpreterSetException
import com.niclas_van_eyk.laravel_make_integration.run.PHPScriptRun

/**
 * With this class we know, that we have a valid Laravel project, so the action
 * that we want to execute will succeed, given that the user provides valid
 * inputs.
 */
open class ArtisanMakeSubCommandActionExecution(
    protected open val command: SubCommand,
    protected open val project: Project,
    protected open val laravelProject: LaravelProject,
    protected open val target: String?
) {
    protected open val directoryResolver: DirectoryResolver
        get() = DirectoryResolver(command.location)

    protected open val targetResolver: TargetResolver
        get() = TargetResolver(directoryResolver)

    protected open val createdFileResolver: CreatedFileResolver
        get() = CreatedFileResolver(laravelProject)

    protected open val ideAdapter: IdeAdapter
        get() = JetbrainsAdapter(project, command)

    fun execute() {
        val initialInput = targetResolver.suggestInitialInputFor(
            target,
            laravelProject.paths.base
        )
        // We don't need to provide any feedback here, since the user either
        // hit cancel or provided no input
        val dialog = ArtisanMakeDialog(project, laravelProject, command, initialInput)

        if (!dialog.showAndGet()) return

        val parameters = ArtisanMakeParameters.fromInput(dialog.input)
        parameters.parameters.add("--no-interaction")

        val commandLine = parameters.humanReadable(command.command)

        var makeResult: PHPScriptRun.Result? = null
        val cancelled = !ProgressManager
            .getInstance()
            .runProcessWithProgressSynchronously({
                val indicator = ProgressIndicatorProvider.getGlobalProgressIndicator()

                if (indicator != null) {
                    indicator.text = "Attaching to PHP interpreter..."
                }

                try {
                    makeResult = laravelProject.artisan.make(command.command, parameters, project)
                } catch (e: NoInterpreterSetException) {
                    var message = "No PHP interpreter found!"
                    message += "\nPlease set one in Settings > Languages & Frameworks > PHP"

                    makeResult = PHPScriptRun.Result(false, arrayListOf(message))
                } catch (e: PhpEditInterpreterExecutionException) {
                    var message = "Could not connect to the configured remote interpreter."

                    if (e.message?.contains("Docker") == true) {
                        message += " Do you need to start your Docker daemon?"
                    }

                    makeResult = PHPScriptRun.Result(false, arrayListOf(message))
                }
            }, commandLine, true, project)

        if (cancelled || makeResult == null) return

        if (makeResult!!.wasFailure) {
            PluginNotifications.error(makeResult!!.log).notify(project)
        }

        val createdFilePath = createdFileResolver.getCreatedFilePath(command, parameters)

        if (createdFilePath == null) {
            PluginNotifications.warning(
                    "The artisan:make run succeeded, but we were unable to locate the newly created file!"
            ).notify(project)
            return
        }

        println("Success! Trying to open '$createdFilePath'...")

        ideAdapter.openFile(createdFilePath)
    }
}