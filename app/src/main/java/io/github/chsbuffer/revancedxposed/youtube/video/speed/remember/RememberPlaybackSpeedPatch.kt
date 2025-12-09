package io.github.chsbuffer.revancedxposed.youtube.video.speed.remember

import app.revanced.extension.youtube.patches.playback.speed.RememberPlaybackSpeedPatch
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.ListPreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.youtube.video.information.VideoInformationPatch
import io.github.chsbuffer.revancedxposed.youtube.video.information.onCreateHook
import io.github.chsbuffer.revancedxposed.youtube.video.information.setPlaybackSpeedClassField
import io.github.chsbuffer.revancedxposed.youtube.video.information.setPlaybackSpeedContainerClassField
import io.github.chsbuffer.revancedxposed.youtube.video.information.setPlaybackSpeedMethod
import io.github.chsbuffer.revancedxposed.youtube.video.information.userSelectedPlaybackSpeedHook
import io.github.chsbuffer.revancedxposed.youtube.video.speed.custom.CustomPlaybackSpeed
import io.github.chsbuffer.revancedxposed.youtube.video.speed.settingsMenuVideoSpeedGroup

val RememberPlaybackSpeed = patch {
    dependsOn(
        VideoInformationPatch,
        CustomPlaybackSpeed
    )

    settingsMenuVideoSpeedGroup.addAll(
        listOf(
            ListPreference(
                key = "revanced_playback_speed_default",
                // Entries and values are set by the extension code based on the actual speeds available.
                entriesKey = null,
                entryValuesKey = null,
                tag = app.revanced.extension.youtube.settings.preference.CustomVideoSpeedListPreference::class.java
            ),
            SwitchPreference("revanced_remember_playback_speed_last_selected"),
            SwitchPreference("revanced_remember_playback_speed_last_selected_toast")
        )
    )

    onCreateHook.add { RememberPlaybackSpeedPatch.newVideoStarted(it) }

    userSelectedPlaybackSpeedHook.add { RememberPlaybackSpeedPatch.userSelectedPlaybackSpeed(it) }

    /*
     * Hook the code that is called when the playback speeds are initialized, and sets the playback speed
     */
    ::initializePlaybackSpeedValuesFingerprint.hookMethod {
        val onItemClickListenerClassField = ::onItemClickListenerClassFieldReference.field
        before {
            val playbackSpeedOverride = RememberPlaybackSpeedPatch.getPlaybackSpeedOverride()
            if (playbackSpeedOverride > 0.0f) {
                onItemClickListenerClassField.get(it.thisObject)
                    .let { setPlaybackSpeedContainerClassField.get(it) }
                    .let { setPlaybackSpeedClassField.get(it) }
                    .let { setPlaybackSpeedMethod(it, playbackSpeedOverride) }
            }
        }
    }
}
