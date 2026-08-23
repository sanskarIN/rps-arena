package `in`.sanskar.rpsarena

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.model.ArenaSettings
import `in`.sanskar.rpsarena.ui.ArenaUiTags
import `in`.sanskar.rpsarena.ui.RpsArenaApp
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RpsArenaUiTest {
    @Test
    fun onboardingEntersHomeScreen() = runComposeUiTest {
        val repository = testRepository(onboardingComplete = false)

        setContent { RpsArenaApp(repository) }

        onNodeWithTag(ArenaUiTags.ONBOARDING_SCREEN).assertExists()
        onNodeWithTag(ArenaUiTags.ENTER_ARENA).performClick()
        onNodeWithTag(ArenaUiTags.HOME_SCREEN).assertExists()
        assertTrue(repository.loadSettings().onboardingComplete)
    }

    @Test
    fun homeCanNavigateToPlayAndBack() = runComposeUiTest {
        val repository = testRepository()

        setContent { RpsArenaApp(repository) }

        onNodeWithTag(ArenaUiTags.HOME_PLAY).performClick()
        onNodeWithTag(ArenaUiTags.PLAY_SCREEN).assertExists()
        onNodeWithTag(ArenaUiTags.PLAY_BACK).performClick()
        onNodeWithTag(ArenaUiTags.HOME_SCREEN).assertExists()
    }

    @Test
    fun gestureCreatesVisibleRoundResult() = runComposeUiTest {
        val repository = testRepository()

        setContent { RpsArenaApp(repository) }

        onNodeWithTag(ArenaUiTags.HOME_PLAY).performClick()
        onNodeWithTag(ArenaUiTags.GESTURE_ROCK).performClick()
        onNodeWithTag(ArenaUiTags.LAST_ROUND).assertExists()
    }

    @Test
    fun reducedMotionTogglePersistsSetting() = runComposeUiTest {
        val repository = testRepository()

        setContent { RpsArenaApp(repository) }

        onNodeWithTag(ArenaUiTags.TOP_SETTINGS).performClick()
        onNodeWithTag(ArenaUiTags.SETTINGS_SCREEN).assertExists()
        onNodeWithTag(ArenaUiTags.SETTINGS_REDUCED_MOTION).performClick()
        waitForIdle()

        assertTrue(repository.loadSettings().reducedMotion)
    }

    @Test
    fun backupExportOpensDialog() = runComposeUiTest {
        val repository = testRepository()

        setContent { RpsArenaApp(repository) }

        onNodeWithTag(ArenaUiTags.TOP_SETTINGS).performClick()
        onNodeWithTag(ArenaUiTags.SETTINGS_EXPORT_BACKUP).performClick()
        onNodeWithTag(ArenaUiTags.EXPORT_DIALOG).assertExists()
    }

    private fun testRepository(onboardingComplete: Boolean = true): ArenaRepository {
        val repository = ArenaRepository(TestArenaStore())
        repository.saveSettings(ArenaSettings(onboardingComplete = onboardingComplete))
        return repository
    }
}
