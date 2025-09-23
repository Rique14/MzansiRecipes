package com.mzansi.recipes.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

// This is the key utility function that creates a new context with the desired language
fun Context.createLocaleContext(language: String?): Context {
    if (language.isNullOrEmpty() || language == "system") {
        // Use the system locale if no language is set
        return this
    }
    val locale = Locale(language)
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
}