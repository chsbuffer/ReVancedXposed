package io.github.chsbuffer.revancedxposed.music.misc.debugging

import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.music.misc.settings.PreferenceScreen
import io.github.chsbuffer.revancedxposed.shared.misc.debugging.EnableDebugging

val EnableDebugging = patch(
    name = "Enable debugging",
    description = "Adds options for debugging and exporting ReVanced logs to the clipboard.",
) {
    EnableDebugging(
        preferenceScreen = PreferenceScreen.MISC
    )
}
