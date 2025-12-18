package io.github.chsbuffer.revancedxposed.reddit.ad.comments

import app.revanced.extension.shared.Logger
import io.github.chsbuffer.revancedxposed.patch

val HideCommentAds = patch(
    description = "Removes ads in the comments.",) {
    ::hideCommentAdsFingerprint.hookMethod {
        before {
            Logger.printDebug { "Hide Comment" }
            it.result = it.args[0]
        }
    }
}