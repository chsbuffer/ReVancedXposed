package io.github.chsbuffer.revancedxposed.youtube.layout.buttons.action

import app.revanced.extension.youtube.patches.components.ButtonsFilter
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.PreferenceScreenPreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.litho.filter.LithoFilter
import io.github.chsbuffer.revancedxposed.youtube.misc.litho.filter.addLithoFilter
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen

val HideButtons = patch(
    name = "Hide video action buttons",
    description = "Adds options to hide action buttons (such as the Download button) under videos.",
) {
    dependsOn(LithoFilter)

    PreferenceScreen.PLAYER.addPreferences(
        PreferenceScreenPreference(
            "revanced_hide_buttons_screen",
            preferences = setOf(
                SwitchPreference("revanced_disable_like_subscribe_glow"),
                SwitchPreference("revanced_hide_ask_button"),
                SwitchPreference("revanced_hide_clip_button"),
                SwitchPreference("revanced_hide_comments_button"),
                SwitchPreference("revanced_hide_download_button"),
                SwitchPreference("revanced_hide_hype_button"),
                SwitchPreference("revanced_hide_like_dislike_button"),
                SwitchPreference("revanced_hide_promote_button"),
                SwitchPreference("revanced_hide_remix_button"),
                SwitchPreference("revanced_hide_report_button"),
                SwitchPreference("revanced_hide_save_button"),
                SwitchPreference("revanced_hide_share_button"),
                SwitchPreference("revanced_hide_shop_button"),
                SwitchPreference("revanced_hide_stop_ads_button"),
                SwitchPreference("revanced_hide_thanks_button"),
            )
        )
    )

    addLithoFilter(ButtonsFilter())
}
