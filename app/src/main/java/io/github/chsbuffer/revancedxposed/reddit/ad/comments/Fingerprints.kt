package io.github.chsbuffer.revancedxposed.reddit.ad.comments

import io.github.chsbuffer.revancedxposed.fingerprint
import org.luckypray.dexkit.query.enums.StringMatchType

val hideCommentAdsFingerprint = fingerprint {
    methodMatcher {
        name("invokeSuspend")
        declaredClass("LoadAdsCombinedCall", StringMatchType.Contains)
    }
}