package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.model.AppLanguage
import `in`.sanskar.rpsarena.model.Difficulty
import `in`.sanskar.rpsarena.model.Gesture
import `in`.sanskar.rpsarena.model.MatchMode
import `in`.sanskar.rpsarena.ui.APP_VERSION
import `in`.sanskar.rpsarena.ui.stringsFor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ArenaStringsTest {
    @Test
    fun HindiCatalogLocalizesGameplayChoices() {
        val english = stringsFor(AppLanguage.ENGLISH)
        val hindi = stringsFor(AppLanguage.HINDI)

        Gesture.entries.forEach { gesture ->
            assertNotEquals(english.gestureLabel(gesture), hindi.gestureLabel(gesture))
        }
        Difficulty.entries.forEach { difficulty ->
            assertNotEquals(english.difficultyLabel(difficulty), hindi.difficultyLabel(difficulty))
        }
        MatchMode.entries.forEach { mode ->
            assertNotEquals(english.modeLabel(mode), hindi.modeLabel(mode))
        }
    }

    @Test
    fun EnglishCatalogKeepsCanonicalGestureLabels() {
        val english = stringsFor(AppLanguage.ENGLISH)

        Gesture.entries.forEach { gesture ->
            assertEquals(gesture.label, english.gestureLabel(gesture))
        }
    }

    @Test
    fun sharedAppVersionUsesSemanticVersionShape() {
        val parts = APP_VERSION.split('.')

        assertEquals(3, parts.size)
        parts.forEach { part -> assertEquals(part.toInt().toString(), part) }
    }
}
