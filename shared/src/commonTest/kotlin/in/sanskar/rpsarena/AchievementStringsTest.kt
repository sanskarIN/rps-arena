package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.model.AppLanguage
import `in`.sanskar.rpsarena.ui.achievementCopy
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AchievementStringsTest {
    private val knownIds = listOf(
        "first_win",
        "ten_rounds",
        "streak_3",
        "streak_7",
        "century",
    )

    @Test
    fun everyKnownAchievementHasDistinctHindiCopy() {
        knownIds.forEach { id ->
            val english = achievementCopy(id, AppLanguage.ENGLISH)
            val hindi = achievementCopy(id, AppLanguage.HINDI)

            assertNotEquals(english.title, hindi.title)
            assertNotEquals(english.description, hindi.description)
            assertTrue(hindi.title.isNotBlank())
            assertTrue(hindi.description.isNotBlank())
        }
    }

    @Test
    fun unknownAchievementUsesSafeFallbackCopy() {
        assertTrue(achievementCopy("future-id", AppLanguage.ENGLISH).title.isNotBlank())
        assertTrue(achievementCopy("future-id", AppLanguage.HINDI).title.isNotBlank())
    }
}
