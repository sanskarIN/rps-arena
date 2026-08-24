package `in`.sanskar.rpsarena.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import `in`.sanskar.rpsarena.model.ArenaSettings

@Composable
fun ArenaTheme(settings: ArenaSettings, content: @Composable () -> Unit) {
    val useDark = if (settings.followSystemTheme) isSystemInDarkTheme() else settings.darkTheme
    MaterialTheme(
        colorScheme = if (useDark) darkColorScheme() else lightColorScheme(),
        content = content,
    )
}
