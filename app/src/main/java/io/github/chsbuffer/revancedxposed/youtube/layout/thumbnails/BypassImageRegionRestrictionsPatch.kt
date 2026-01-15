package io.github.chsbuffer.revancedxposed.youtube.layout.thumbnails

import app.revanced.extension.youtube.patches.BypassImageRegionRestrictionsPatch.overrideImageURL
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.imageurlhook.addImageUrlHook
import io.github.chsbuffer.revancedxposed.youtube.misc.imageurlhook.cronetImageUrlHookPatch
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen

val BypassImageRegionRestrictionsPatch = patch(
    name = "Bypass image region restrictions",
    description = "Adds an option to use a different host for user avatar and channel images " +
            "and can fix missing images that are blocked in some countries.",
) {
    dependsOn(
        cronetImageUrlHookPatch,
    )

    PreferenceScreen.MISC.addPreferences(
        SwitchPreference("revanced_bypass_image_region_restrictions"),
    )

    // A priority hook is not needed, as the image urls of interest are not modified
    // by AlternativeThumbnails or any other patch in this repo.
    addImageUrlHook(::overrideImageURL)
}
