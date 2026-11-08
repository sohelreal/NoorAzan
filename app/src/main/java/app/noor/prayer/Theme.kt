package app.noor.prayer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val HeaderGreen = Color(0xFF1B5E20)
val HeaderGreenLight = Color(0xFF2E7D32)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF1B5E20),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F3E4),
    onPrimaryContainer = Color(0xFF0B3D0E),
    secondary = Color(0xFF2E7D32),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F5E9),
    onSecondaryContainer = Color(0xFF0B3D0E),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF14201A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF14201A),
    surfaceVariant = Color(0xFFEFF6EF),
    onSurfaceVariant = Color(0xFF3D4D41),
    outline = Color(0xFF9DB2A0),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF0B2E0E),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFD4EED5),
    secondary = Color(0xFFA5D6A7),
    onSecondary = Color(0xFF0B2E0E),
    secondaryContainer = Color(0xFF244A28),
    onSecondaryContainer = Color(0xFFD4EED5),
    background = Color(0xFF0E1510),
    onBackground = Color(0xFFE2EDE4),
    surface = Color(0xFF131C15),
    onSurface = Color(0xFFE2EDE4),
    surfaceVariant = Color(0xFF1E2A21),
    onSurfaceVariant = Color(0xFFB9C9BC),
    outline = Color(0xFF5E7362),
)

@Composable
fun NoorTheme(content: @Composable () -> Unit) {
    val dark = when (Prefs.theme) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, content = content)
}
