package io.github.chsbuffer.revancedxposed.youtube.misc.privacy

import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.shared.misc.privacy.SanitizeSharingLinks
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen

val SanitizeSharingLinks = patch(
    name = "Sanitize sharing links",
    description = "Removes the tracking query parameters from shared links."
) {
    SanitizeSharingLinks(preferenceScreen = PreferenceScreen.MISC)
}