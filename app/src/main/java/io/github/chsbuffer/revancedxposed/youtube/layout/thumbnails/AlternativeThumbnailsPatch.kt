package io.github.chsbuffer.revancedxposed.youtube.layout.thumbnails

import app.revanced.extension.youtube.patches.AlternativeThumbnailsPatch.handleCronetFailure
import app.revanced.extension.youtube.patches.AlternativeThumbnailsPatch.handleCronetSuccess
import app.revanced.extension.youtube.patches.AlternativeThumbnailsPatch.overrideImageURL
import app.revanced.extension.youtube.settings.preference.AlternativeThumbnailsAboutDeArrowPreference
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.ListPreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.NonInteractivePreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.TextPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.imageurlhook.addImageUrlErrorCallbackHook
import io.github.chsbuffer.revancedxposed.youtube.misc.imageurlhook.addImageUrlHook
import io.github.chsbuffer.revancedxposed.youtube.misc.imageurlhook.addImageUrlSuccessCallbackHook
import io.github.chsbuffer.revancedxposed.youtube.misc.imageurlhook.cronetImageUrlHookPatch
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen

val AlternativeThumbnailsPatch = patch(
    name = "Alternative thumbnails",
    description = "Adds options to replace video thumbnails using the DeArrow API or image captures from the video.",
) {
    dependsOn(
        cronetImageUrlHookPatch,
    )

    val entries = "revanced_alt_thumbnail_options_entries"
    val values = "revanced_alt_thumbnail_options_entry_values"
    PreferenceScreen.ALTERNATIVE_THUMBNAILS.addPreferences(
        ListPreference(
            key = "revanced_alt_thumbnail_home",
            entriesKey = entries,
            entryValuesKey = values
        ),
        ListPreference(
            key = "revanced_alt_thumbnail_subscription",
            entriesKey = entries,
            entryValuesKey = values
        ),
        ListPreference(
            key = "revanced_alt_thumbnail_library",
            entriesKey = entries,
            entryValuesKey = values
        ),
        ListPreference(
            key = "revanced_alt_thumbnail_player",
            entriesKey = entries,
            entryValuesKey = values
        ),
        ListPreference(
            key = "revanced_alt_thumbnail_search",
            entriesKey = entries,
            entryValuesKey = values
        ),
        NonInteractivePreference(
            "revanced_alt_thumbnail_dearrow_about",
            // Custom about preference with link to the DeArrow website.
            tag = AlternativeThumbnailsAboutDeArrowPreference::class.java,
            selectable = true,
        ),
        SwitchPreference("revanced_alt_thumbnail_dearrow_connection_toast"),
        TextPreference("revanced_alt_thumbnail_dearrow_api_url"),
        NonInteractivePreference("revanced_alt_thumbnail_stills_about"),
        SwitchPreference("revanced_alt_thumbnail_stills_fast"),
        ListPreference("revanced_alt_thumbnail_stills_time"),
    )

    addImageUrlHook(::overrideImageURL)
    addImageUrlSuccessCallbackHook(::handleCronetSuccess)
    addImageUrlErrorCallbackHook(::handleCronetFailure)
}
