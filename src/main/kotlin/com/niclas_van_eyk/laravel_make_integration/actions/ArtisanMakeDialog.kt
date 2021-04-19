package com.niclas_van_eyk.laravel_make_integration.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.EditorTextField
import com.niclas_van_eyk.laravel_make_integration.laravel.LaravelProject
import com.niclas_van_eyk.laravel_make_integration.services.project.Command
import com.niclas_van_eyk.laravel_make_integration.laravel.artisan.CommandAutocompleteTextField
import com.niclas_van_eyk.laravel_make_integration.services.LaravelMakeIntegrationProjectService
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * The dialog that prompts the user for the name + flags to pass to the make-command.
 *
 * [CommandAutocompleteTextField] is probably more interesting here.
 */
class ArtisanMakeDialog(
        val project: Project,
        val laravelProject: LaravelProject,
        val command: SubCommand,
        val initialInput: InitialInputSuggestion
): DialogWrapper(project) {
    private lateinit var editor: EditorTextField

    val input: String get() = editor.text

    init {
        title = command.description
        init()
    }

    override fun createCenterPanel(): JComponent? {
        val panel = JPanel(BorderLayout(0, 4))

        val commands = retrieveCommands()
        val matchingCommand = commands.firstOrNull { it.name == command.asArtisanCommand }
        val options = matchingCommand?.definition?.options?.values?.toMutableList() ?: mutableListOf()

        editor = CommandAutocompleteTextField(project, options, initialInput.toInputString)
        editor.setPreferredWidth(250)
        panel.add(LabeledComponent.create(editor, command.capitalized), BorderLayout.SOUTH)
        return panel
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return editor
    }

    private fun retrieveCommands(): MutableList<Command> {
        val service = project.getService(LaravelMakeIntegrationProjectService::class.java)

        return if (!service.hasCommands) {
            mutableListOf()
        } else {
            service.commands.commands
        }
    }
}