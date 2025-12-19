package io.github.chsbuffer.revancedxposed.youtube.layout.autocaptions

import app.revanced.extension.youtube.patches.DisableAutoCaptionsPatch
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen

val AutoCaptionsPatch = patch(
    name = "Disable auto captions",
    description = "Adds an option to disable captions from being automatically enabled.",
) {
    PreferenceScreen.PLAYER.addPreferences(
        SwitchPreference("revanced_disable_auto_captions"),
    )

    ::subtitleTrackFingerprint.hookMethod {
        before {
            if (DisableAutoCaptionsPatch.disableAutoCaptions()) {
                it.result = true
            }
        }
    }

    mapOf(
        ::startVideoInformerFingerprint to 0,
        ::storyboardRendererDecoderRecommendedLevelFingerprint to 1
    ).forEach { (fingerprint, enabled) ->
        fingerprint.hookMethod {
            before {
                DisableAutoCaptionsPatch.setCaptionsButtonStatus(enabled != 0)
            }
        }
    }
}