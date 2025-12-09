package io.github.chsbuffer.revancedxposed.youtube.video.quality

import app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.BasePreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.PreferenceCategory
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.PreferenceScreenPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen

val settingsMenuVideoQualityGroup = mutableSetOf<BasePreference>()

val VideoQuality = patch(
    name = "Video quality",
    description = "Adds options to set default video qualities and always use the advanced video quality menu."
) {
    dependsOn(
        RememberVideoQuality,
        AdvancedVideoQualityMenu,
        VideoQualityDialogButtonPatch
    )

    PreferenceScreen.VIDEO.addPreferences(
        // Keep the preferences organized together.
        PreferenceCategory(
            key = "revanced_01_video_key", // Dummy key to force the quality preferences first.
            titleKey = null,
            sorting = PreferenceScreenPreference.Sorting.UNSORTED,
            tag = NoTitlePreferenceCategory::class.java,
            preferences = settingsMenuVideoQualityGroup
        )
    )
}