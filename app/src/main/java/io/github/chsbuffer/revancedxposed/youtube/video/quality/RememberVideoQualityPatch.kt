package io.github.chsbuffer.revancedxposed.youtube.video.quality

import app.revanced.extension.youtube.patches.playback.quality.RememberVideoQualityPatch
import io.github.chsbuffer.revancedxposed.getIntField
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.scopedHook
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.ListPreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.playertype.PlayerTypeHook
import io.github.chsbuffer.revancedxposed.youtube.shared.VideoQualityReceiver
import io.github.chsbuffer.revancedxposed.youtube.shared.videoQualityChangedFingerprint
import io.github.chsbuffer.revancedxposed.youtube.video.information.VideoInformationPatch
import io.github.chsbuffer.revancedxposed.youtube.video.information.onCreateHook

val RememberVideoQuality = patch {
    dependsOn(
        VideoInformationPatch,
        PlayerTypeHook,
    )

    settingsMenuVideoQualityGroup.addAll(
        listOf(
            ListPreference(
                key = "revanced_video_quality_default_mobile",
                entriesKey = "revanced_video_quality_default_entries",
                entryValuesKey = "revanced_video_quality_default_entry_values"
            ),
            ListPreference(
                key = "revanced_video_quality_default_wifi",
                entriesKey = "revanced_video_quality_default_entries",
                entryValuesKey = "revanced_video_quality_default_entry_values"
            ),
            SwitchPreference("revanced_remember_video_quality_last_selected"),

            ListPreference(
                key = "revanced_shorts_quality_default_mobile",
                entriesKey = "revanced_shorts_quality_default_entries",
                entryValuesKey = "revanced_shorts_quality_default_entry_values",
            ),
            ListPreference(
                key = "revanced_shorts_quality_default_wifi",
                entriesKey = "revanced_shorts_quality_default_entries",
                entryValuesKey = "revanced_shorts_quality_default_entry_values"
            ),
            SwitchPreference("revanced_remember_shorts_quality_last_selected"),
            SwitchPreference("revanced_remember_video_quality_last_selected_toast")
        )
    )

    onCreateHook.add { controller ->
        RememberVideoQualityPatch.newVideoStarted(controller)
    }

    // Inject a call to remember the selected quality for Shorts.
    ::videoQualityItemOnClickFingerprint.hookMethod {
        before { param ->
            RememberVideoQualityPatch.userChangedQuality(param.args[2] as Int)
        }
    }

    // Inject a call to remember the user selected quality for regular videos.
    ::videoQualityChangedFingerprint.hookMethod(scopedHook(::VideoQualityReceiver.member) {
        before { param ->
            val selectedQualityIndex = param.args[0].getIntField("a")
            RememberVideoQualityPatch.userChangedQuality(selectedQualityIndex)
        }
    })
}