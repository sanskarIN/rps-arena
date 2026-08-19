package in.sanskar.rpsarena.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import in.sanskar.rpsarena.data.PlatformStore
import in.sanskar.rpsarena.ui.RpsArenaApp

fun main() = application {
    PlatformStore.initialize()
    Window(onCloseRequest = ::exitApplication, title = "RPS Arena") {
        RpsArenaApp()
    }
}
