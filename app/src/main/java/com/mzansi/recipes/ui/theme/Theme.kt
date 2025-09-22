package com.mzansi.recipes.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF2E7D32)

@Composable
fun MzansiTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) darkColorScheme(primary = Green) else lightColorScheme(primary = Green)
    MaterialTheme(colorScheme = colorScheme, content = content)
}