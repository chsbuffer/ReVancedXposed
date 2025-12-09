package io.github.chsbuffer.revancedxposed.youtube.video.speed

import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.BasePreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.PreferenceCategory
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen
import io.github.chsbuffer.revancedxposed.youtube.video.speed.button.PlaybackSpeedButton
import io.github.chsbuffer.revancedxposed.youtube.video.speed.custom.CustomPlaybackSpeed
import io.github.chsbuffer.revancedxposed.youtube.video.speed.remember.RememberPlaybackSpeed

/**
 * Speed menu settings.  Used to organize all speed related settings together.
 */
internal val settingsMenuVideoSpeedGroup = mutableSetOf<BasePreference>()

@Suppress("unused")
val PlaybackSpeed = patch(
    name = "Playback speed",
    description = "Adds options to customize available playback speeds, set a default playback speed, " +
            "and show a speed dialog button in the video player.",
) {
    dependsOn(
        CustomPlaybackSpeed,
        RememberPlaybackSpeed,
        PlaybackSpeedButton,
    )

    PreferenceScreen.VIDEO.addPreferences(
        PreferenceCategory(
            key = "revanced_zz_video_key", // Dummy key to force the speed settings last.
            titleKey = null,
            sorting = Sorting.UNSORTED,
            tag = app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory::class.java,
            preferences = settingsMenuVideoSpeedGroup
        )
    )
}

