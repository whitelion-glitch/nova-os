package com.novaos.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NovaDarkColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF5EA5FF),
    secondary = androidx.compose.ui.graphics.Color(0xFF7F96FF),
    background = androidx.compose.ui.graphics.Color(0xFF0B0F17),
    surface = androidx.compose.ui.graphics.Color(0xFF111A2A),
    onPrimary = androidx.compose.ui.graphics.Color(0xFFF5F7FF),
    onBackground = androidx.compose.ui.graphics.Color(0xFFF5F7FF),
    onSurface = androidx.compose.ui.graphics.Color(0xFFF5F7FF),
)

@Composable
fun NovaOsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NovaDarkColorScheme,
        content = content,
    )
}
