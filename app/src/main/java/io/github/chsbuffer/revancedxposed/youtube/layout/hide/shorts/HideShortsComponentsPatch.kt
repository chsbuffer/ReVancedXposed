package io.github.chsbuffer.revancedxposed.youtube.layout.hide.shorts

import app.revanced.extension.youtube.patches.components.ShortsFilter
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.litho.filter.LithoFilter
import io.github.chsbuffer.revancedxposed.youtube.misc.litho.filter.addLithoFilter
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen

val HideShortsComponents = patch(
    name = "Hide Shorts components",
    description = "Adds options to hide components related to Shorts.",
) {
    dependsOn(LithoFilter)

    PreferenceScreen.SHORTS.addPreferences(
        SwitchPreference("revanced_hide_shorts_home"),
        SwitchPreference("revanced_hide_shorts_search"),
        SwitchPreference("revanced_hide_shorts_subscriptions"),
        SwitchPreference("revanced_hide_shorts_history"),
        // TODO: revanced_shorts_player_screen
    )

    addLithoFilter(ShortsFilter())
}