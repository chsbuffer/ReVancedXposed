package io.github.chsbuffer.revancedxposed.youtube.video.speed.button

import app.revanced.extension.youtube.videoplayer.PlaybackSpeedDialogButton
import io.github.chsbuffer.revancedxposed.R
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.playercontrols.ControlInitializer
import io.github.chsbuffer.revancedxposed.youtube.misc.playercontrols.PlayerControls
import io.github.chsbuffer.revancedxposed.youtube.misc.playercontrols.addBottomControl
import io.github.chsbuffer.revancedxposed.youtube.misc.playercontrols.initializeBottomControl
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen
import io.github.chsbuffer.revancedxposed.youtube.video.information.VideoInformationPatch
import io.github.chsbuffer.revancedxposed.youtube.video.information.userSelectedPlaybackSpeedHook
import io.github.chsbuffer.revancedxposed.youtube.video.information.videoSpeedChangedHook
import io.github.chsbuffer.revancedxposed.youtube.video.speed.custom.CustomPlaybackSpeed

val PlaybackSpeedButton = patch(
    description = "Adds the option to display playback speed dialog button in the video player.",
) {
    dependsOn(
        CustomPlaybackSpeed,
        PlayerControls,
        VideoInformationPatch,
    )

    PreferenceScreen.PLAYER.addPreferences(
        SwitchPreference("revanced_playback_speed_dialog_button"),
    )

    addBottomControl(R.layout.revanced_playback_speed_dialog_button)
    initializeBottomControl(
        ControlInitializer(
            R.id.revanced_playback_speed_dialog_button_container,
            PlaybackSpeedDialogButton::initializeButton,
            PlaybackSpeedDialogButton::setVisibility,
            PlaybackSpeedDialogButton::setVisibilityImmediate,
            PlaybackSpeedDialogButton::setVisibilityNegatedImmediate,
        )
    )

    videoSpeedChangedHook.add { PlaybackSpeedDialogButton.videoSpeedChanged(it) }
    userSelectedPlaybackSpeedHook.add { PlaybackSpeedDialogButton.videoSpeedChanged(it) }
}

