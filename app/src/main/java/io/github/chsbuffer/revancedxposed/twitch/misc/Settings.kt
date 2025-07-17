package io.github.chsbuffer.revancedxposed.twitch.misc

import app.revanced.extension.twitch.settings.preference.CustomPreferenceCategory
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.BasePreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.BasePreferenceScreen
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.PreferenceCategory
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.PreferenceScreenPreference

val preferences = mutableSetOf<BasePreference>()

fun addSettingPreference(screen: BasePreference) {
    preferences += screen
}

/**
 * Preference screens patches should add their settings to.
 */
@Suppress("ktlint:standard:property-naming")
internal object PreferenceScreen : BasePreferenceScreen() {
    val ADS = CustomScreen("revanced_ads_screen")
    val CHAT = CustomScreen("revanced_chat_screen")
    val MISC = CustomScreen("revanced_misc_screen")

    internal class CustomScreen(key: String) : Screen(key) {
        /* Categories */
        val GENERAL = CustomCategory("revanced_general_category")
        val OTHER = CustomCategory("revanced_other_category")
        val CLIENT_SIDE = CustomCategory("revanced_client_ads_category")
        val SURESTREAM = CustomCategory("revanced_surestream_ads_category")

        internal inner class CustomCategory(key: String) : Category(key) {
            /* For Twitch, we need to load our CustomPreferenceCategory class instead of the default one. */
            override fun transform(): PreferenceCategory = PreferenceCategory(
                key,
                preferences = preferences,
                tag = CustomPreferenceCategory::class.java,
            )
        }
    }

    override fun commit(screen: PreferenceScreenPreference) {
        addSettingPreference(screen)
    }
}
