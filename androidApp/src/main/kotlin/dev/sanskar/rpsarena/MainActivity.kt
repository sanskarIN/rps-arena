package dev.sanskar.rpsarena

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.sanskar.rpsarena.data.AppRepository
import dev.sanskar.rpsarena.data.KeyValueStore
import dev.sanskar.rpsarena.state.AppController
import dev.sanskar.rpsarena.ui.RpsArenaApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val controller = AppController(AppRepository(AndroidKeyValueStore(this)))
        setContent {
            RpsArenaApp(
                controller = controller,
                openUrl = { url ->
                    runCatching {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                },
            )
        }
    }
}

private class AndroidKeyValueStore(context: Context) : KeyValueStore {
    private val preferences = context.getSharedPreferences("rps_arena", Context.MODE_PRIVATE)

    override fun getString(key: String): String? = preferences.getString(key, null)

    override fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    override fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    override fun clear() {
        preferences.edit().clear().apply()
    }
}
