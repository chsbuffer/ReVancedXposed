package io.github.chsbuffer.revancedxposed.music.audio.exclusiveaudio

import de.robv.android.xposed.XC_MethodReplacement
import io.github.chsbuffer.revancedxposed.patch

val EnableExclusiveAudioPlayback = patch(
    name = "Enable exclusive audio playback",
    description = "Enables the option to play audio without video.",
) {
    ::AllowExclusiveAudioPlaybackFingerprint.hookMethod(XC_MethodReplacement.returnConstant(true))
}