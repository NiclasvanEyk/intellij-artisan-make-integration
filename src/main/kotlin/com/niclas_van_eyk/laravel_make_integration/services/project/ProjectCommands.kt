package com.niclas_van_eyk.laravel_make_integration.services.project

import com.intellij.openapi.project.Project
import com.niclas_van_eyk.laravel_make_integration.laravel.LaravelApplicationIntrospecter
import com.niclas_van_eyk.laravel_make_integration.laravel.LaravelProject

class ProjectCommands(val laravelProject: LaravelProject, val project: Project) {
    var commands = mutableListOf<Command>()
    private val introspecter = LaravelApplicationIntrospecter(laravelProject, project)

    fun load(onSuccess: (commands: List<Command>) -> Unit) {
        introspecter.fetchCommandInfo {
            commands.clear()
            commands.addAll(it.commands.sortedBy { command -> command.name })
            onSuccess(commands)
        }
    }
}