package com.niclas_van_eyk.laravel_make_integration.ide

interface IdeAdapter {
    fun openFile(path: String)

    fun getUserInput(initialValue: String?): String?

    fun log(key: String, message: String)
}