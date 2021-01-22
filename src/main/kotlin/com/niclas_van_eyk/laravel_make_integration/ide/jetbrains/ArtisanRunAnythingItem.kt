package com.niclas_van_eyk.laravel_make_integration.ide.jetbrains

import com.intellij.ide.actions.runAnything.items.RunAnythingItemBase
import com.niclas_van_eyk.laravel_make_integration.LaravelIcons
import com.niclas_van_eyk.laravel_make_integration.laravel.artisan.Command

class ArtisanRunAnythingItem(
        private val command: Command
): RunAnythingItemBase(
        "${ArtisanRunAnythingProvider.ARTISAN_COMMAND} ${command.name}",
        LaravelIcons.LaravelLogo
) {
    override fun getDescription() = command.description
}