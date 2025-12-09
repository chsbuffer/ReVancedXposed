package io.github.chsbuffer.revancedxposed.youtube.misc.debugging

import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.shared.misc.debugging.EnableDebugging
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen

val EnableDebugging = patch(
    name = "Enable debugging",
    description = "Adds options for debugging and exporting ReVanced logs to the clipboard.",
) {
    EnableDebugging(
        preferenceScreen = PreferenceScreen.MISC,
        additionalDebugPreferences = listOf(SwitchPreference("revanced_debug_protobuffer"))
    )
}
