package `in`.sanskar.rpsarena.ui

import androidx.compose.ui.window.ComposeUIViewController
import `in`.sanskar.rpsarena.data.PlatformStore
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    PlatformStore.initialize()
    return ComposeUIViewController {
        RpsArenaApp()
    }
}
