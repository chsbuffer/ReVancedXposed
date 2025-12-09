package io.github.chsbuffer.revancedxposed.reddit.misc.tracking.url

import io.github.chsbuffer.revancedxposed.patch

val SanitizeUrlQuery = patch(
    name = "Sanitize sharing links",
    description = "Removes the tracking query parameters from shared links."
) {
    ::shareLinkFormatterFingerprint.hookMethod {
        before {
            it.result = it.args[0]
        }
    }
}