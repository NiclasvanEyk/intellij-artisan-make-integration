package com.niclas_van_eyk.laravel_make_integration.ide.jetbrains

import com.niclas_van_eyk.laravel_make_integration.laravel.artisan.Command as ArtisanCommand

class RunAnythingArtisanCommand(val definition: ArtisanCommand, val commandLine: String)