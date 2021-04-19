package com.niclas_van_eyk.laravel_make_integration.services.project

import com.intellij.openapi.project.Project
import com.niclas_van_eyk.laravel_make_integration.laravel.LaravelApplicationIntrospecter
import com.niclas_van_eyk.laravel_make_integration.laravel.LaravelProject

class ProjectConfig(val laravelProject: LaravelProject, val project: Project) {
    var config: Any? = null
    private val introspecter = LaravelApplicationIntrospecter(laravelProject, project)

    fun load() {
        introspecter.fetchConfigInfo { config = it }
    }
}