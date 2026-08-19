package dev.sanskar.rpsarena.desktop

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.sanskar.rpsarena.data.AppRepository
import dev.sanskar.rpsarena.data.KeyValueStore
import dev.sanskar.rpsarena.state.AppController
import dev.sanskar.rpsarena.ui.RpsArenaApp
import java.awt.Desktop
import java.net.URI
import java.util.prefs.Preferences

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RPS Arena",
        state = rememberWindowState(width = 1100.dp, height = 760.dp),
    ) {
        val controller = remember {
            AppController(AppRepository(DesktopKeyValueStore()))
        }
        RpsArenaApp(
            controller = controller,
            openUrl = { url ->
                runCatching {
                    if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(URI(url))
                }
            },
        )
    }
}

private class DesktopKeyValueStore : KeyValueStore {
    private val preferences = Preferences.userRoot().node("dev/sanskar/rpsarena")

    override fun getString(key: String): String? = preferences.get(key, null)

    override fun putString(key: String, value: String) {
        preferences.put(key, value)
    }

    override fun remove(key: String) {
        preferences.remove(key)
    }

    override fun clear() {
        preferences.clear()
    }
}
