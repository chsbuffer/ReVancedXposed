package io.github.chsbuffer.revancedxposed.youtube.video.quality

import app.revanced.extension.youtube.videoplayer.VideoQualityDialogButton
import io.github.chsbuffer.revancedxposed.R
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.playercontrols.ControlInitializer
import io.github.chsbuffer.revancedxposed.youtube.misc.playercontrols.PlayerControls
import io.github.chsbuffer.revancedxposed.youtube.misc.playercontrols.addBottomControl
import io.github.chsbuffer.revancedxposed.youtube.misc.playercontrols.initializeBottomControl
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen

val VideoQualityDialogButtonPatch = patch(
    description = "Adds the option to display video quality dialog button in the video player.",
) {
    dependsOn(
        RememberVideoQuality,
        PlayerControls,
    )

    PreferenceScreen.PLAYER.addPreferences(
        SwitchPreference("revanced_video_quality_dialog_button"),
    )

    addBottomControl(R.layout.revanced_video_quality_dialog_button_container)
    initializeBottomControl(
        ControlInitializer(
            R.id.revanced_video_quality_dialog_button_container,
            VideoQualityDialogButton::initializeButton,
            VideoQualityDialogButton::setVisibility,
            VideoQualityDialogButton::setVisibilityImmediate,
            VideoQualityDialogButton::setVisibilityNegatedImmediate
        )
    )
}
