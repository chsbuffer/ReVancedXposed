package io.github.chsbuffer.revancedxposed.youtube.video.audio

import app.revanced.extension.shared.patches.ForceOriginalAudioPatch
import app.revanced.extension.shared.settings.preference.ForceOriginalAudioSwitchPreference
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.shared.misc.debugging.experimentalBooleanFeatureFlagFingerprint
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen
import io.github.chsbuffer.revancedxposed.youtube.shared.mainActivityOnCreateFingerprint

val ForceOriginalAudio = patch(
    name = "Force original audio",
    description = "Adds an option to always use the original audio track.",
) {
    PreferenceScreen.VIDEO.addPreferences(
        SwitchPreference(
            key = "revanced_force_original_audio",
            tag = ForceOriginalAudioSwitchPreference::class.java
        )
    )

    ::mainActivityOnCreateFingerprint.hookMethod {
        before {
            app.revanced.extension.youtube.patches.ForceOriginalAudioPatch.setEnabled()
        }
    }

    // Disable feature flag that ignores the default track flag
    // and instead overrides to the user region language.
    ::experimentalBooleanFeatureFlagFingerprint.hookMethod {
        after {
            if (it.args[1] == AUDIO_STREAM_IGNORE_DEFAULT_FEATURE_FLAG) {
                it.result =
                    ForceOriginalAudioPatch.ignoreDefaultAudioStream(it.result as Boolean)
            }
        }
    }

    val getFormatStreamModelGetter = ::getFormatStreamModelGetter.dexMethodList
    val getIsDefaultAudioTrackFingerprint = getFormatStreamModelGetter[0]
    val getAudioTrackIdFingerprint = getFormatStreamModelGetter[1]
    val getAudioTrackDisplayNameFingerprint = getFormatStreamModelGetter[2]

    getIsDefaultAudioTrackFingerprint.hookMethod {
        val getAudioTrackIdMethod = getAudioTrackIdFingerprint.toMethod()
        val getAudioTrackDisplayNameMethod = getAudioTrackDisplayNameFingerprint.toMethod()
        after {
            it.result = ForceOriginalAudioPatch.isDefaultAudioStream(
                it.result as Boolean,
                getAudioTrackIdMethod(it.thisObject) as String?,
                getAudioTrackDisplayNameMethod(it.thisObject) as String?
            )
        }
    }
}
