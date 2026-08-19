package `in`.sanskar.rpsarena.model

/**
 * A local-only player identity. Profiles are intentionally small and contain no account,
 * authentication, cloud, or telemetry identifiers.
 */
data class LocalProfile(
    val id: String,
    val displayName: String,
)

data class LocalProfilesState(
    val profiles: List<LocalProfile>,
    val activeProfileId: String,
) {
    val activeProfile: LocalProfile
        get() = profiles.firstOrNull { it.id == activeProfileId }
            ?: profiles.firstOrNull()
            ?: DEFAULT_LOCAL_PROFILE

    companion object {
        val DEFAULT_LOCAL_PROFILE = LocalProfile(
            id = "profile-1",
            displayName = "Player 1",
        )

        fun default(): LocalProfilesState = LocalProfilesState(
            profiles = listOf(DEFAULT_LOCAL_PROFILE),
            activeProfileId = DEFAULT_LOCAL_PROFILE.id,
        )
    }
}
