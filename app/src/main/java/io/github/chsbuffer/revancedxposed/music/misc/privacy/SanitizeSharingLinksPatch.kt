package io.github.chsbuffer.revancedxposed.music.misc.privacy

import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.music.misc.settings.PreferenceScreen
import io.github.chsbuffer.revancedxposed.shared.misc.privacy.SanitizeSharingLinks

val SanitizeSharingLinks = patch(
    name = "Sanitize sharing links",
    description = "Removes the tracking query parameters from shared links."
) {
    SanitizeSharingLinks(
        preferenceScreen = PreferenceScreen.MISC,
        replaceMusicLinksWithYouTube = true
    )
}