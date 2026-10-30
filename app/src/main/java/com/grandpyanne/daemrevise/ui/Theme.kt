package com.grandpyanne.daemrevise.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Navy and amber: an aviation feel without being loud.
private val Navy = Color(0xFF1F3A5F)
private val NavyLight = Color(0xFFA9C4E8)
private val Amber = Color(0xFFB7791F)
private val AmberLight = Color(0xFFF2B134)

private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6F2),
    onPrimaryContainer = Color(0xFF0E2238),
    secondary = Amber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCEBC8),
    onSecondaryContainer = Color(0xFF3D2800),
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE7EBF0),
    onSurfaceVariant = Color(0xFF44474E),
)

private val DarkColors = darkColorScheme(
    primary = NavyLight,
    onPrimary = Color(0xFF0E2238),
    primaryContainer = Color(0xFF2B4A72),
    onPrimaryContainer = Color(0xFFDCE6F2),
    secondary = AmberLight,
    onSecondary = Color(0xFF3D2800),
    secondaryContainer = Color(0xFF5C4100),
    onSecondaryContainer = Color(0xFFFCEBC8),
    background = Color(0xFF111418),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1A1E23),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF2A2F36),
    onSurfaceVariant = Color(0xFFC4C6CF),
)

@Composable
fun DaemReviseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}

/** Fixed colours used for meaning (right/wrong, status) and for each unit's accent. */
object AppColors {
    val Correct = Color(0xFF2E7D32)
    val Wrong = Color(0xFFC62828)
    val Safety = Color(0xFFB7791F)
    val Later = Color(0xFF7A808A)

    /** Deep night-sky colours for the study score card. */
    val NightTop = Color(0xFF1B2A55)
    val NightBottom = Color(0xFF2E2A6B)
    val ScoreLilac = Color(0xFFB9A8FF)

    /** Accent colours given to units in the order they appear on the home screen. */
    private val unitAccents = listOf(
        Color(0xFF3D6FB6), // steel blue
        Color(0xFF7B5CC4), // violet
        Color(0xFF2F9E8F), // teal
        Color(0xFFD1793A), // orange
        Color(0xFF4F8A3C), // green
        Color(0xFFB0464F), // red
        Color(0xFF3A8FC4), // sky
        Color(0xFF8A6D3B), // bronze
    )

    fun unitAccent(index: Int): Color = unitAccents[index % unitAccents.size]
}
