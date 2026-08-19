package `in`.sanskar.rpsarena.ui

object UiTags {
    const val ONBOARDING_ENTER = "onboarding-enter"
    const val HOME_PLAY = "home-play"
    const val LAST_ROUND = "last-round"

    fun gesture(name: String): String = "gesture-${name.lowercase()}"
}
