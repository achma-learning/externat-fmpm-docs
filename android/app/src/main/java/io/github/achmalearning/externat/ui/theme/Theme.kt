package io.github.achmalearning.externat.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import io.github.achmalearning.externat.data.ThemeMode

// Palette de repli : vert-bleu (teal) comme la couverture du guide de stage FMPM.
private val LightColors = lightColorScheme(
    primary = Color(0xFF00695C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF9EF2E1),
    onPrimaryContainer = Color(0xFF00201B),
    secondary = Color(0xFF4A635D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCE8E0),
    onSecondaryContainer = Color(0xFF06201A),
    tertiary = Color(0xFF2E7D32),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFB8F0B4),
    onTertiaryContainer = Color(0xFF002204),
    background = Color(0xFFF5FBF8),
    onBackground = Color(0xFF171D1B),
    surface = Color(0xFFF5FBF8),
    onSurface = Color(0xFF171D1B),
    surfaceVariant = Color(0xFFDAE5E1),
    onSurfaceVariant = Color(0xFF3F4946),
    outline = Color(0xFF6F7976),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF82D5C5),
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF005045),
    onPrimaryContainer = Color(0xFF9EF2E1),
    secondary = Color(0xFFB1CCC4),
    onSecondary = Color(0xFF1C352F),
    secondaryContainer = Color(0xFF334B45),
    onSecondaryContainer = Color(0xFFCCE8E0),
    tertiary = Color(0xFF9CD49A),
    onTertiary = Color(0xFF00390B),
    tertiaryContainer = Color(0xFF155219),
    onTertiaryContainer = Color(0xFFB8F0B4),
    background = Color(0xFF0E1513),
    onBackground = Color(0xFFDEE4E1),
    surface = Color(0xFF0E1513),
    onSurface = Color(0xFFDEE4E1),
    surfaceVariant = Color(0xFF3F4946),
    onSurfaceVariant = Color(0xFFBEC9C5),
    outline = Color(0xFF899390),
)

/** Vrai quand l'app est affichée en thème sombre (sert à l'inversion optionnelle des pages PDF). */
val LocalDarkTheme = staticCompositionLocalOf { false }

@Composable
fun isAppInDarkTheme(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
fun ExternatTheme(dark: Boolean, dynamicColor: Boolean, content: @Composable () -> Unit) {
    val colors: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        dark -> DarkColors
        else -> LightColors
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalDarkTheme provides dark) {
        MaterialTheme(colorScheme = colors, content = content)
    }
}
