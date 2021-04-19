package com.niclas_van_eyk.laravel_make_integration.laravel

import com.google.gson.GsonBuilder
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.run.PhpEditInterpreterExecutionException
import com.niclas_van_eyk.laravel_make_integration.ide.jetbrains.PluginNotifications
import com.niclas_van_eyk.laravel_make_integration.services.project.LaravelConsoleApplication
import com.niclas_van_eyk.laravel_make_integration.services.project.RouteListEntry
import com.niclas_van_eyk.laravel_make_integration.run.InterpreterInference
import com.niclas_van_eyk.laravel_make_integration.run.PHPScriptRun
import com.niclas_van_eyk.laravel_make_integration.services.project.EventListenerPair

class CommandRunInfo(
    val namespace: String,
    val command: String?,
    val options: List<String> = emptyList()
)

class LaravelApplicationIntrospecter(
    private val laravelProject: LaravelProject,
    private val project: Project
) {
    fun fetchCommandInfo(onSuccess: (app: LaravelConsoleApplication) -> Unit) {
        val jsonFormatOption = "--format=json"

        receiveCommandOutput(
            "Scanning Artisan commands",
            CommandRunInfo("list", null, listOf(jsonFormatOption))
        ) { output ->
            val rawJson = extractJson(output) ?: return@receiveCommandOutput
            val json = rawJson
                // For some reason an empty argument list gets serialized
                // to an empty array instead of an empty object. Maybe
                // because of associative arrays in php are kinda the
                // same as JSON objects.
                .replace(Regex("\"options\":\\[\\]]"), "\"options\":{}")
                .replace(Regex("\"arguments\":\\[\\]]"), "\"arguments\":{}")
                // These are getting added by Docker I think
                .replace(Regex("\n"), "")

            val application = GsonBuilder()
                .create()
                .fromJson(json, LaravelConsoleApplication::class.java)

            onSuccess(application)
        }
    }

    fun fetchConfigInfo(onSuccess: (result: Any) -> Unit) {
        receiveCommandOutput(
            "Scanning Laravel config",
            CommandRunInfo("tinker", null, listOf("--execute", "echo json_encode(config()->all())"))
        ) { output ->
            val json = extractJson(output) ?: return@receiveCommandOutput
            onSuccess(GsonBuilder().create().fromJson(json, Any::class.java))
        }
    }

    fun fetchRouteInfo(onSuccess: (app: List<RouteListEntry>) -> Unit) {
        receiveCommandOutput(
            "Scanning Laravel routes",
            CommandRunInfo("route", "list", listOf("--json"))
        ) { output ->
            val json = extractJson(output) ?: return@receiveCommandOutput

            val routes = GsonBuilder()
                .create()
                .fromJson(json, Array<RouteListEntry>::class.java)

            onSuccess(routes.toList())
        }
    }

    fun fetchEventsInfo(onSuccess: (pairs: List<EventListenerPair>) -> Unit) {
        receiveCommandOutput(
            "Scanning Laravel events",
            CommandRunInfo("event", "list")
        ) { output ->
            val pairs = output.lines()
                .filter { it.startsWith("| ") }
                .flatMap { it.split("|") }
                .map { it.trim() }
                .filter { it.contains("\\") }
                .chunked(2)
                .map {
                    val event = PhpIndex.getInstance(project).getClassesByFQN(it[0]).firstOrNull()
                    val listener = PhpIndex.getInstance(project).getClassesByFQN(it[1]).firstOrNull()

                    return@map if (event != null && listener != null) {
                        EventListenerPair(event, listener)
                    } else null
                }
                .filterNotNull()

            onSuccess(pairs)
        }
    }

    private fun extractJson(output: String): String? {
        val beginOfJson = output.indexOfAny(listOf("{", "["))
        val endOfJson = output.lastIndexOfAny(listOf("]", "}"))

        if (beginOfJson == -1 || endOfJson == -1) {
            // Maybe we should handle this at a higher level, but
            // we'll often this even happens
            PluginNotifications.error(
                "Something went wrong while trying to parse the JSON output!",
                "No JSON found"
            ).notify(project)
            return null
        }

        return output.substring(beginOfJson, endOfJson + 1).trim()
    }

    private fun receiveCommandOutput(
        title: String,
        commandRunInfo: CommandRunInfo,
        onSuccess: (result: String) -> Unit,
    ) {
        if (! InterpreterInference(project).hasInterpreter()) {
            // We cannot execute anything here, so we need to skip it instead of throwing
            // an exception as https://github.com/NiclasvanEyk/jetbrains-laravel-make-integration/issues/28
            // showed
            return
        }

        ProgressManager.getInstance().run(object: Task.Backgroundable(project, title, true, ALWAYS_BACKGROUND) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                var commandScanResult: PHPScriptRun.Result

                try {
                    commandScanResult = laravelProject.artisan.execute(
                            project,
                            commandRunInfo.namespace,
                            commandRunInfo.command,
                            commandRunInfo.options
                    )
                } catch (e: PhpEditInterpreterExecutionException) {
                    var message = "Could not connect to the configured remote interpreter."

                    if (e.message?.contains("Docker") == true) {
                        message += " Maybe you need to start your Docker daemon?"
                    }

                    commandScanResult = PHPScriptRun.Result(false, arrayListOf(message))
                }

                if (commandScanResult.wasFailure) {
                    indicator.stop()
                    return
                }

                // First the command line arguments are logged. If Docker is used, they contain a '[', which
                // trips up our magnificent strategy for detecting the JSON output
                val output = commandScanResult.log.substringAfter("\n")

                onSuccess(output)
            }
        })
    }
}