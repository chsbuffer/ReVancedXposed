package io.github.chsbuffer.revancedxposed.meta

import android.app.Application
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chsbuffer.revancedxposed.BaseHook
import io.github.chsbuffer.revancedxposed.meta.ads.HideAds

class MetaHook(app: Application, lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook(app, lpparam) {
    override val patches = arrayOf(HideAds)
}