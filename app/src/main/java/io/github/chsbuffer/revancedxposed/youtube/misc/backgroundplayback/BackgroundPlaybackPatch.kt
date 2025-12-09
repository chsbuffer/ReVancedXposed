package io.github.chsbuffer.revancedxposed.youtube.misc.backgroundplayback

import app.revanced.extension.youtube.patches.BackgroundPlaybackPatch
import de.robv.android.xposed.XC_MethodReplacement.returnConstant
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.scopedHook
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.litho.filter.featureFlagCheck
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen

val BackgroundPlayback = patch(
    name = "Remove background playback restrictions",
    description = "Removes restrictions on background playback, including playing kids videos in the background.",
) {

    PreferenceScreen.SHORTS.addPreferences(
        SwitchPreference("revanced_shorts_disable_background_playback"),
    )

    ::backgroundPlaybackManagerFingerprint.hookMethod {
        after {
            it.result = BackgroundPlaybackPatch.isBackgroundPlaybackAllowed(it.result as Boolean)
        }
    }
    ::backgroundPlaybackManagerShortsFingerprint.hookMethod {
        after {
            it.result =
                BackgroundPlaybackPatch.isBackgroundShortsPlaybackAllowed(it.result as Boolean)
        }
    }

    // Enable background playback option in YouTube settings
    ::backgroundPlaybackSettingsSubFingerprint.hookMethod(returnConstant(true))

    // Force allowing background play for Shorts.
    ::shortsBackgroundPlaybackFeatureFlagFingerprint.hookMethod(returnConstant(true))

    // Force allowing background play for videos labeled for kids.
    ::kidsBackgroundPlaybackPolicyControllerFingerprint.hookMethod(returnConstant(Unit))

    // Fix PiP buttons not working after locking/unlocking device screen.
    runCatching {
        ::pipInputConsumerFeatureFlagFingerprint.hookMethod(scopedHook(::featureFlagCheck.member) {
            before { if (it.args[0] == PIP_INPUT_CONSUMER_FEATURE_FLAG) it.result = false }
        })
    }
}