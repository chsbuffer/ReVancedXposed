package io.github.chsbuffer.revancedxposed.reddit

import io.github.chsbuffer.revancedxposed.reddit.ad.general.HideAds
import io.github.chsbuffer.revancedxposed.reddit.misc.tracking.url.SanitizeUrlQuery

val RedditPatches = arrayOf(
    HideAds,
    SanitizeUrlQuery,
)