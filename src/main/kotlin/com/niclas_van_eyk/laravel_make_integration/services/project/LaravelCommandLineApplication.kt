package com.niclas_van_eyk.laravel_make_integration.services.project

import com.google.gson.annotations.SerializedName

data class LaravelConsoleApplication(
    @SerializedName("application") val application : ApplicationDescription,
    @SerializedName("commands") val commands : List<Command>
)

data class ApplicationDescription(
    @SerializedName("name") val name : String,
    @SerializedName("version") val version : String
)

data class Command(
    @SerializedName("name") val name : String,
    @SerializedName("usage") val usage : List<String>,
    @SerializedName("description") val description : String,
    @SerializedName("help") val help : String,
    @SerializedName("definition") val definition : CommandDefinition,
    @SerializedName("hidden") val hidden : Boolean
)

data class CommandDefinition(
    @SerializedName("arguments") val arguments: Map<String, Argument>,
    @SerializedName("options") val options: Map<String, Option>
)

data class Argument(
    @SerializedName("name") val name: String,
    @SerializedName("is_required") val isRequired: Boolean,
    @SerializedName("is_array") val isArray: Boolean,
    @SerializedName("description") val description: String
//    @SerializedName("default") val default: String?
)

data class Option(
    @SerializedName("name") val name : String,
    @SerializedName("shortcut") val shortcut : String?,
    @SerializedName("accept_value") val acceptValue : Boolean,
    @SerializedName("is_value_required") val isValueRequired : Boolean,
    @SerializedName("is_multiple") val isMultiple : Boolean,
    @SerializedName("description") val description : String
//    @SerializedName("default") val default : String?
) {
    var type: OptionType = if (!acceptValue) OptionType.Flag else OptionType.String
}

/**
 * Defines the type of an [Option].
 *
 * This could be helpful to provide further auto-completion, e.g. for the --model parameters
 * of observers if we can somehow retrieve a list of all Models in the project.
 *
 * At the moment they are only shown in the auto-completion list and [OptionType.Flag] is used
 * to find out, whether or not an option takes an argument.
 */
enum class OptionType {
    /**
     * The option can only be passed as a flag, like --verbose
     */
    Flag,

    /**
     * The following types are based on well known parameters, as we cannot infer the
     * data types from the command description only.
     * See [WellKnownCommandInformation]
     */

    /**
     * The option takes some argument, like --foo=bar
     */
    String,

    /**
     * The option takes some argument, like --foo=App/Models/User
     */
    Class,

    /**
     * The option takes some argument, like --foo=123
     */
    Integer,

    /**
     * The option takes some argument, like --foo=123.456
     */
    Float,
}
