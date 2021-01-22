package com.niclas_van_eyk.laravel_make_integration.ide.jetbrains

import com.intellij.ide.actions.runAnything.activity.RunAnythingProviderBase
import com.intellij.ide.actions.runAnything.items.RunAnythingItem
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys.PROJECT
import com.niclas_van_eyk.laravel_make_integration.LaravelIcons
import com.niclas_van_eyk.laravel_make_integration.services.LaravelMakeIntegrationProjectService
import com.niclas_van_eyk.laravel_make_integration.laravel.artisan.Command as ArtisanCommand

class ArtisanRunAnythingProvider: RunAnythingProviderBase<RunAnythingArtisanCommand>() {
    companion object {
        const val ARTISAN_COMMAND = "artisan"
    }

    override fun getCommand(command: RunAnythingArtisanCommand) = command.commandLine
    override fun getIcon(command: RunAnythingArtisanCommand) = LaravelIcons.LaravelLogo
    override fun getCompletionGroupTitle() = "Artisan Commands" // If this is not set completion will not work!
    override fun getHelpGroupTitle() = "Laravel"
    override fun getHelpCommand() = ARTISAN_COMMAND
    override fun getHelpCommandPlaceholder() = "$helpCommand <command>"
    override fun getHelpIcon() = LaravelIcons.LaravelLogo
    override fun getMainListItem(dataContext: DataContext, value: RunAnythingArtisanCommand): RunAnythingItem
            = ArtisanRunAnythingItem(value.definition)

    override fun execute(dataContext: DataContext, value: RunAnythingArtisanCommand) {
        println("Running '${value.definition.name}'...")
    }

    override fun findMatchingValue(dataContext: DataContext, pattern: String): RunAnythingArtisanCommand? {
        if (!pattern.startsWith(helpCommand)) return null

        val commands = getProjectCommandsFrom(dataContext) ?: emptyList()
        val command = pattern.split(' ').getOrElse(1) { "" }

        return commands
                .filter { it.name.startsWith(command) }
                .map { RunAnythingArtisanCommand(it, "$helpCommand ${it.name}") }
                .firstOrNull()
    }

    override fun getValues(dataContext: DataContext, pattern: String): MutableCollection<RunAnythingArtisanCommand> {
        val commands = getProjectCommandsFrom(dataContext) ?: emptyList()

        return when {
            pattern.startsWith(helpCommand) -> {
                val command = pattern.split(' ').getOrElse(1) { "" }
                commands
                        .filter { it.name.startsWith(command) }
                        .map { RunAnythingArtisanCommand(it, "$helpCommand ${it.name}") }.toMutableList()
            }
            pattern.isNotBlank() && helpCommand.startsWith(pattern) ->
                commands.map { RunAnythingArtisanCommand(it, "$helpCommand ${it.name}") }.toMutableList()
            else -> mutableListOf()
        }
    }

    private fun getProjectCommandsFrom(dataContext: DataContext): List<ArtisanCommand>? {
        val project = dataContext.getData(PROJECT) ?: return null

        val laravelProject = project.getService(LaravelMakeIntegrationProjectService::class.java)

        if (laravelProject == null || !laravelProject.hasCommands) return null

        return laravelProject.commands.commands
    }
}