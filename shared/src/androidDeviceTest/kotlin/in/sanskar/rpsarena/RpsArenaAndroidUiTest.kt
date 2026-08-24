package `in`.sanskar.rpsarena

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.data.ArenaStore
import `in`.sanskar.rpsarena.model.ArenaSettings
import `in`.sanskar.rpsarena.ui.ArenaUiTags
import `in`.sanskar.rpsarena.ui.RpsArenaApp
import org.junit.Rule
import org.junit.Test

class RpsArenaAndroidUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun onboardingEntersHomeScreen() {
        val repository = testRepository(onboardingComplete = false)

        composeRule.setContent { RpsArenaApp(repository) }

        composeRule.onNodeWithTag(ArenaUiTags.ONBOARDING_SCREEN).assertExists()
        composeRule.onNodeWithTag(ArenaUiTags.ENTER_ARENA).performClick()
        composeRule.onNodeWithTag(ArenaUiTags.HOME_SCREEN).assertExists()
    }

    @Test
    fun gestureCreatesVisibleRoundResult() {
        val repository = testRepository()

        composeRule.setContent { RpsArenaApp(repository) }

        composeRule.onNodeWithTag(ArenaUiTags.HOME_PLAY).performClick()
        composeRule.onNodeWithTag(ArenaUiTags.GESTURE_ROCK).performClick()
        composeRule.onNodeWithTag(ArenaUiTags.LAST_ROUND).assertExists()
    }

    private fun testRepository(onboardingComplete: Boolean = true): ArenaRepository {
        val repository = ArenaRepository(InMemoryAndroidArenaStore())
        repository.saveSettings(ArenaSettings(onboardingComplete = onboardingComplete))
        return repository
    }
}

private class InMemoryAndroidArenaStore : ArenaStore {
    private val values = mutableMapOf<String, String>()

    override fun getString(key: String, defaultValue: String): String = values[key] ?: defaultValue

    override fun putString(key: String, value: String) {
        values[key] = value
    }
}
