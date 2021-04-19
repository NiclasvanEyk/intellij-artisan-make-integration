package com.niclas_van_eyk.laravel_make_integration.ide.jetbrains

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.niclas_van_eyk.laravel_make_integration.LaravelMakeIntegrationBundle
import com.niclas_van_eyk.laravel_make_integration.actions.SubCommand
import com.niclas_van_eyk.laravel_make_integration.ide.IdeAdapter

/**
 * Used to contain the dependence on the jetbrains platform.
 *
 * Unsure about how much sense this makes.
 */
class JetbrainsAdapter(
    private val project: Project,
    private val meta: SubCommand
): IdeAdapter {
    override fun openFile(path: String) {
        val file = LocalFileSystem.getInstance().refreshAndFindFileByPath(path)

        if (file != null) {
            ApplicationManager.getApplication().runReadAction {
                OpenFileDescriptor(project, file).navigate(true)
            }
        }
    }

    override fun getUserInput(initialValue: String?): String? {
        val initial = initialValue ?: ""

        val input = Messages.showInputDialog(
            project,
            meta.description,
            meta.capitalized,
            null,
                initial,
            null,
            // this way the cursor is at the end
            TextRange(initial.length, initial.length)
        )

        if (input.isNullOrEmpty() || input.trim() == initial.trim()) return null

        return input
    }

    override fun log(key: String, message: String) {
        LaravelMakeIntegrationBundle.message(key, message)
    }
}
