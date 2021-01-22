package com.niclas_van_eyk.laravel_make_integration.laravel.artisan

import com.google.gson.GsonBuilder
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import com.niclas_van_eyk.laravel_make_integration.laravel.LaravelProject
import com.niclas_van_eyk.laravel_make_integration.services.LaravelMakeIntegrationProjectService

/**
 * This class runs [parseArtisanMakeCommandNames] and [parseArtisanCommandFromHelp],
 * to get a structured list of all make:* commands and their parameters/options/flags,
 * etc.
 *
 * This most likely takes a few seconds, as we execute `php artisan make:command --help`
 * several times and connecting to the php interpreter can take some time (e.g.) if it
 * is inside a docker container that has to be started first). The use of this class
 * is mainly to schedule these operations and display a nice progress bar at the bottom
 * of the ide in the meantime.
 *
 * @see LaravelMakeIntegrationProjectService.commands
 */
class ProjectCommands(val laravelProject: LaravelProject, val project: Project) {
    var commands = mutableListOf<Command>()

    fun inferFromHelp() {
        val progressManager = ProgressManager.getInstance()

        progressManager.run(object: Backgroundable(project, "Scanning for Artisan commands", true, ALWAYS_BACKGROUND) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                val commandScanResult = laravelProject.artisan.execute(
                    project,
                    "list",
                    null,
                    listOf("--format=json")
                )

                if (commandScanResult.wasFailure) {
                    print(commandScanResult.log) // TODO
                    indicator.stop()
                    return
                }

                val output = commandScanResult.log
                val json = output.substring(output.indexOf("{\"application\":"))
                    // For some reason an empty argument list gets serialized
                    // to an empty array instead of an empty object. Maybe
                    // because of associative arrays in php are kinda the
                    // same as JSON objects.
                    .replace(Regex("\"options\":\\[\\]]"), "\"options\":{}")
                    .replace(Regex("\"arguments\":\\[\\]]"), "\"arguments\":{}")
                    // These are getting added by Docker I think
                    .replace(Regex("\n"), "")
                    // No idea where this comes from, maybe also Docker?
                    .removeSuffix("Done!")

                val application = GsonBuilder()
                    .create()
                    .fromJson<LaravelConsoleApplication>(
                        json, LaravelConsoleApplication::class.java
                    )

                commands.addAll(application.commands)
            }
        })
    }
}