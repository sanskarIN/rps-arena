package `in`.sanskar.rpsarena.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import `in`.sanskar.rpsarena.data.PlatformStore
import `in`.sanskar.rpsarena.ui.RpsArenaApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    PlatformStore.initialize()
    ComposeViewport {
        RpsArenaApp()
    }
}
