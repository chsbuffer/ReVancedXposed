package io.github.chsbuffer.revancedxposed.youtube.layout.startupshortsreset

import app.revanced.extension.youtube.patches.DisableResumingStartupShortsPlayerPatch
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.scopedHook
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen

val DisableResumingShortsOnStartup = patch(
    name = "Disable resuming Shorts on startup",
    description = "Adds an option to disable the Shorts player from resuming on app startup when Shorts were last being watched.",
) {
    PreferenceScreen.SHORTS.addPreferences(
        SwitchPreference("revanced_disable_resuming_shorts_player"),
    )

    ::userWasInShortsFingerprint.hookMethod(scopedHook(::userWasInShortsBuilderFingerprint.member) {
        val arg = ::userWasInShortsBuilderFingerprint.dexMethod.paramTypeNames.indexOf("boolean")
        before {
            it.args[arg] =
                DisableResumingStartupShortsPlayerPatch.disableResumingStartupShortsPlayer(it.args[arg] as Boolean)
        }
    })

    ::userWasInShortsConfigFingerprint.hookMethod {
        before {
            if (DisableResumingStartupShortsPlayerPatch.disableResumingStartupShortsPlayer())
                it.result = false
        }
    }
}