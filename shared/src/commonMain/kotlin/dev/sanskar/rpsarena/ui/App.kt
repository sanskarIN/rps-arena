package dev.sanskar.rpsarena.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sanskar.rpsarena.state.AppController

private enum class Screen(val label: String, val symbol: String) {
    HOME("Home", "⌂"),
    PLAY("Play", "▶"),
    STATS("Stats", "▥"),
    HISTORY("History", "↺"),
    SETTINGS("Settings", "⚙"),
    ABOUT("About", "ⓘ"),
}

@Composable
fun RpsArenaApp(
    controller: AppController,
    openUrl: (String) -> Unit = {},
) {
    var screen by remember { mutableStateOf(Screen.HOME) }

    RpsTheme(controller.settings.theme) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            if (maxWidth >= 760.dp) {
                Row(Modifier.fillMaxSize()) {
                    NavigationRail(modifier = Modifier.width(104.dp)) {
                        Screen.entries.forEach { item ->
                            NavigationRailItem(
                                selected = screen == item,
                                onClick = { screen = item },
                                icon = { Text(item.symbol) },
                                label = { Text(item.label) },
                                alwaysShowLabel = true,
                            )
                        }
                    }
                    Box(Modifier.weight(1f)) {
                        ScreenContent(screen, controller, openUrl) { screen = it }
                    }
                }
            } else {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            Screen.entries.take(5).forEach { item ->
                                NavigationBarItem(
                                    selected = screen == item,
                                    onClick = { screen = item },
                                    icon = { Text(item.symbol) },
                                    label = { Text(item.label) },
                                )
                            }
                        }
                    },
                ) { padding ->
                    Box(Modifier.fillMaxSize().padding(padding)) {
                        ScreenContent(screen, controller, openUrl) { screen = it }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenContent(
    screen: Screen,
    controller: AppController,
    openUrl: (String) -> Unit,
    navigate: (Screen) -> Unit,
) {
    when (screen) {
        Screen.HOME -> HomeScreen(controller, onPlay = { navigate(Screen.PLAY) }, onAbout = { navigate(Screen.ABOUT) })
        Screen.PLAY -> PlayScreen(controller)
        Screen.STATS -> StatsScreen(controller)
        Screen.HISTORY -> HistoryScreen(controller)
        Screen.SETTINGS -> SettingsScreen(controller)
        Screen.ABOUT -> AboutScreen(openUrl)
    }
}
