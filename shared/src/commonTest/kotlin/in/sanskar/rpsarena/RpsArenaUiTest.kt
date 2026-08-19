package `in`.sanskar.rpsarena

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.data.KeyValueStore
import `in`.sanskar.rpsarena.ui.RpsArenaApp
import `in`.sanskar.rpsarena.ui.UiTags
import kotlin.test.Test

class RpsArenaUiTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun onboardingToFirstRoundPrimaryJourney() = runComposeUiTest {
        setContent {
            RpsArenaApp(ArenaRepository(UiMemoryStore()))
        }

        onNodeWithTag(UiTags.ONBOARDING_ENTER)
            .assertExists()
            .performClick()
        waitForIdle()

        onNodeWithTag(UiTags.HOME_PLAY)
            .assertExists()
            .performClick()
        waitForIdle()

        onNodeWithTag(UiTags.gesture("ROCK"))
            .assertExists()
            .performClick()
        waitForIdle()

        onNodeWithTag(UiTags.LAST_ROUND).assertExists()
    }
}

private class UiMemoryStore : KeyValueStore {
    private val values = mutableMapOf<String, String>()

    override fun getString(key: String, defaultValue: String): String = values[key] ?: defaultValue

    override fun putString(key: String, value: String) {
        values[key] = value
    }
}
