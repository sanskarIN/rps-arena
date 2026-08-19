package `in`.sanskar.rpsarena.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import `in`.sanskar.rpsarena.model.ArenaSettings

private val ArenaLightColors = lightColorScheme(
    primary = Color(0xFF4A4FB5),
    onPrimary = Color.White,
    secondary = Color(0xFF5C5F72),
    tertiary = Color(0xFF76546C),
    background = Color(0xFFFDFBFF),
    surface = Color(0xFFFDFBFF),
    surfaceVariant = Color(0xFFE4E1EC),
)

private val ArenaDarkColors = darkColorScheme(
    primary = Color(0xFFC0C1FF),
    onPrimary = Color(0xFF191A63),
    secondary = Color(0xFFC5C5D7),
    tertiary = Color(0xFFE6BAD5),
    background = Color(0xFF1B1B1F),
    surface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFF46464F),
)

private val ArenaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun ArenaTheme(settings: ArenaSettings, content: @Composable () -> Unit) {
    val useDark = if (settings.followSystemTheme) isSystemInDarkTheme() else settings.darkTheme
    MaterialTheme(
        colorScheme = if (useDark) ArenaDarkColors else ArenaLightColors,
        shapes = ArenaShapes,
        content = content,
    )
}
