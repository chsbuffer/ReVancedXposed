package io.github.chsbuffer.revancedxposed.reddit

import android.app.Application
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chsbuffer.revancedxposed.BaseHook
import io.github.chsbuffer.revancedxposed.reddit.ad.general.HideAds
import io.github.chsbuffer.revancedxposed.reddit.misc.tracking.url.SanitizeUrlQuery

val RedditPatches = arrayOf(
    HideAds,
    SanitizeUrlQuery,
)

class RedditHook(app: Application, lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook(app, lpparam) {
    override val patches = RedditPatches
}