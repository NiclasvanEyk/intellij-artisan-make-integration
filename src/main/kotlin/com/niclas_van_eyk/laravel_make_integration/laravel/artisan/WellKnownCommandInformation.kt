package com.niclas_van_eyk.laravel_make_integration.laravel.artisan

import com.niclas_van_eyk.laravel_make_integration.services.project.Command
import com.niclas_van_eyk.laravel_make_integration.services.project.OptionType

/**
 * Information, that can not be inferred by the --help information of a command, but is
 * helpful.
 *
 * E.g. [OptionType.Class] for make:controller --model. The --help info does not show that
 * we have to pass a model here, but we can "enhance" the statically inferred info with
 * manually gathered ones to hint the user that a class needs to be passed here or an int or
 * a more precise data type.
 *
 * @deprecated for now, until I figure out a way how to mutate the lists properly
 */
class WellKnownCommandInformation {
    var wellKnownOptionTypes: MutableMap<String, MutableMap<String, OptionType>> = HashMap()

    init {
        addOptionType("make:controller", "--model", OptionType.Class)
        addOptionType("make:controller", "--parent", OptionType.Class)

        addOptionType("make:factory", "--model", OptionType.Class)

        addOptionType("make:listener", "--event", OptionType.Class)

        addOptionType("make:observer", "--model", OptionType.Class)

        addOptionType("make:policy", "--model", OptionType.Class)
    }

    fun addOptionType(command: String, option: String, type: OptionType) {
        val optionTypesForCommand = wellKnownOptionTypes.getOrPut(command, { HashMap() })
        optionTypesForCommand[option] = type
    }

    fun updateOptionTypes(commands: List<Command>): List<Command> {
        return commands.map { command ->
            val typesForCommandOptions = wellKnownOptionTypes[command.name] ?: return@map command

            command.definition.options.values.forEach {
                if (typesForCommandOptions.containsKey(it.name)) {
                    it.type = typesForCommandOptions[it.name]!!
                } else if (typesForCommandOptions.containsKey(it.shortcut)) {
                    it.type = typesForCommandOptions[it.shortcut]!!
                }
            }

            return@map command
        }
    }
}
