package `in`.sanskar.rpsarena

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.ui.RpsArenaApp
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RpsArenaUiTest {
    @Test
    fun onboardingCanReachPrimaryGameplay() = runComposeUiTest {
        val repository = memoryRepository()

        setContent { RpsArenaApp(repository) }

        onNodeWithText("Welcome to RPS Arena").assertExists()
        onNodeWithText("Enter the Arena").performClick()
        onNodeWithText("Choose your arena").assertExists()
        onNodeWithText("Play").performClick()
        onNodeWithText("Choose a gesture").assertExists()
        onNodeWithText("Rock").assertExists()
        onNodeWithText("Paper").assertExists()
        onNodeWithText("Scissors").assertExists()
    }

    @Test
    fun languageSettingUpdatesCoreNavigationCopy() = runComposeUiTest {
        val repository = memoryRepository()

        setContent { RpsArenaApp(repository) }

        onNodeWithText("Enter the Arena").performClick()
        onNodeWithText("Settings").performClick()
        onNodeWithText("Language").assertExists()
        onNodeWithText("Hindi").performClick()
        onNodeWithText("सेटिंग्स").assertExists()
        onNodeWithText("हिन्दी").assertExists()
    }

    @Test
    fun HindiSettingLocalizesGameplayAndAchievements() = runComposeUiTest {
        val repository = memoryRepository()

        setContent { RpsArenaApp(repository) }

        onNodeWithText("Enter the Arena").performClick()
        onNodeWithText("Settings").performClick()
        onNodeWithText("Hindi").performClick()
        onNodeWithText("← वापस").performClick()
        onNodeWithText("खेलें").performClick()
        onNodeWithText("पत्थर").assertExists()
        onNodeWithText("कागज़").assertExists()
        onNodeWithText("कैंची").assertExists()
        onNodeWithText("← वापस").performClick()
        onNodeWithText("उपलब्धियाँ").performClick()
        onNodeWithText("पहली जीत").assertExists()
        onNodeWithText("अपना पहला राउंड जीतें").assertExists()
    }

    @Test
    fun settingsExposeBackupAndConfirmedResetControls() = runComposeUiTest {
        val repository = memoryRepository()

        setContent { RpsArenaApp(repository) }

        onNodeWithText("Enter the Arena").performClick()
        onNodeWithText("Settings").performClick()
        onNodeWithText("Prepare backup").assertExists()
        onNodeWithText("Import backup").assertExists()
        onNodeWithText("Reset local data").performClick()
        onNodeWithText("Confirm reset").assertExists()
        onNodeWithText("Cancel").assertExists()
    }

    private fun memoryRepository(): ArenaRepository {
        val values = mutableMapOf<String, String>()
        return ArenaRepository(
            readString = { key, defaultValue -> values[key] ?: defaultValue },
            writeString = { key, value -> values[key] = value },
        )
    }
}
