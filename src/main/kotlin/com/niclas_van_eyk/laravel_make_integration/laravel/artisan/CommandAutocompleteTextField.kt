package com.niclas_van_eyk.laravel_make_integration.laravel.artisan

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.TextFieldWithAutoCompletionListProvider
import com.niclas_van_eyk.laravel_make_integration.services.project.Option

/**
 * An input that suggests the user the passed commands for better discoverability.
 */
class CommandAutocompleteTextField(
    project: Project,
    availableCommands: MutableList<Option>,
    initialInput: String = ""
) : TextFieldWithAutoCompletion<Option>(
        project,
        CommandAutocompletionProvider(withoutUnnecessaryOptions(availableCommands)),
        false,
        initialInput
) {
    companion object {
        /**
         * Filters out options, that make no sense to display everytime.
         *
         * These options are part of nearly every command, so the user most likely
         * knows them. Additionally flags like --help and --version would prevent
         * the creation of any files, so the user most likely does not want to pass
         * them, since this extension should generate files and not display versions.
         */
        fun withoutUnnecessaryOptions(availableCommands: MutableList<Option>): MutableList<Option> {
            return availableCommands.filter {
                !listOf(
                        "--help",
                        "--version",
                        "--verbose",
                        "--ansi",
                        "--no-ansi",
                        "--no-interaction",
                        "--quiet"
                ).contains(it.name)
            }.toMutableList()
        }
    }

    init {
        setCaretPosition(initialInput.split(" ").first().length)
    }

    private class CommandAutocompletionProvider(
            availableOptions: MutableCollection<Option>
    ) : TextFieldWithAutoCompletionListProvider<Option>(availableOptions) {
        override fun getLookupString(option: Option): String {
            return if (!option.acceptValue) option.name
                   else option.name + "="
        }

        override fun getTypeText(option: Option): String {
            return if (option.acceptValue) "Option" else "Flag" // option.type.name
        }

        override fun createLookupBuilder(option: Option): LookupElementBuilder {
            val lookup = super.createLookupBuilder(option)
                    .withTailText(if (option.description.isNotBlank()) "  " + option.description else null, true)
                    .withPresentableText(option.name)

            if (option.shortcut != null) {
                lookup.withLookupStrings(option.shortcut.split("|"))
            }

            return lookup
        }
    }
}