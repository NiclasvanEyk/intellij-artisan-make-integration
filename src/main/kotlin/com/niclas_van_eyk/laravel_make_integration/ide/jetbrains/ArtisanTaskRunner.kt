package com.niclas_van_eyk.laravel_make_integration.ide.jetbrains

import com.intellij.task.ProjectTask
import com.intellij.task.ProjectTaskRunner

class ArtisanTaskRunner: ProjectTaskRunner() {
    override fun canRun(projectTask: ProjectTask): Boolean {
        return true
    }


}