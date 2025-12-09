package io.github.chsbuffer.revancedxposed.youtube.interaction.copyvideourl

import app.revanced.extension.youtube.videoplayer.CopyVideoUrlButton
import app.revanced.extension.youtube.videoplayer.CopyVideoUrlTimestampButton
import io.github.chsbuffer.revancedxposed.R
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.playercontrols.ControlInitializer
import io.github.chsbuffer.revancedxposed.youtube.misc.playercontrols.PlayerControls
import io.github.chsbuffer.revancedxposed.youtube.misc.playercontrols.addBottomControl
import io.github.chsbuffer.revancedxposed.youtube.misc.playercontrols.initializeBottomControl
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen
import io.github.chsbuffer.revancedxposed.youtube.video.information.VideoInformationPatch

val CopyVideoUrl = patch(
    name = "Copy video URL",
    description = "Adds options to display buttons in the video player to copy video URLs.",
) {
    dependsOn(
        PlayerControls,
        VideoInformationPatch,
    )

    PreferenceScreen.PLAYER.addPreferences(
        SwitchPreference("revanced_copy_video_url"),
        SwitchPreference("revanced_copy_video_url_timestamp"),
    )

    addBottomControl(R.layout.revanced_copy_video_url_button)
    initializeBottomControl(
        ControlInitializer(
            R.id.revanced_copy_video_url_timestamp_button,
            CopyVideoUrlTimestampButton::initializeButton,
            CopyVideoUrlTimestampButton::setVisibility,
            CopyVideoUrlTimestampButton::setVisibilityImmediate,
            CopyVideoUrlTimestampButton::setVisibilityNegatedImmediate
        )
    )
    initializeBottomControl(
        ControlInitializer(
            R.id.revanced_copy_video_url_button,
            CopyVideoUrlButton::initializeButton,
            CopyVideoUrlButton::setVisibility,
            CopyVideoUrlButton::setVisibilityImmediate,
            CopyVideoUrlButton::setVisibilityNegatedImmediate
        )
    )
}