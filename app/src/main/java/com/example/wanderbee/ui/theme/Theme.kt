package com.example.wanderbee.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WanderBeeColorScheme = lightColorScheme(
    primary = PrimaryButton,         // Blue buttons
    onPrimary = OnPrimaryButton,     // Text on buttons

    secondary = Highlight,           // Yellow for highlighted text
    onSecondary = AppBackground,     // Contrast on yellow (optional)

    tertiary = Tertiary,             // Green usage
    onTertiary = Color.White,        // Assuming white for visibility

    error = ErrorColor,              // Red error indicators
    onError = Color.White,           // Text on red

    background = AppBackground,      // Main background
    onBackground = Subheading,       // Text on background

    surface = AppBackground,         // Same as background
    onSurface = Subheading,          // General text

    surfaceVariant = InputField,     // Text field backgrounds
    onSurfaceVariant = Subheading,   // Hint or label text

    outline = InputField             // Borders, lines
)

@Composable
fun WanderBeeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WanderBeeColorScheme,
        typography = Typography,
        content = content
    )
}
