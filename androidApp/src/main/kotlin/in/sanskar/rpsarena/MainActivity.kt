package in.sanskar.rpsarena

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import in.sanskar.rpsarena.data.PlatformStore
import in.sanskar.rpsarena.ui.RpsArenaApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlatformStore.initialize(this)
        setContent { RpsArenaApp() }
    }
}
