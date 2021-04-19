package com.niclas_van_eyk.laravel_make_integration.services.project

import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.niclas_van_eyk.laravel_make_integration.laravel.LaravelApplicationIntrospecter
import com.niclas_van_eyk.laravel_make_integration.laravel.LaravelProject

class EventListenerPair(val event: PhpClass, val listener: PhpClass)

class ProjectEvents (val laravelProject: LaravelProject, val project: Project) {
    var eventsAndListeners: List<EventListenerPair> = emptyList()
    private val introspecter = LaravelApplicationIntrospecter(laravelProject, project)

    fun load(onSuccess: (pairs: List<EventListenerPair>) -> Unit = {}) {
//        introspecter.fetchEventsInfo {
//            eventsAndListeners = it
//            onSuccess(eventsAndListeners)
//        }
    }
}