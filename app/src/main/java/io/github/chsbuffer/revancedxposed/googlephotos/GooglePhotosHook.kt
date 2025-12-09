package io.github.chsbuffer.revancedxposed.googlephotos

import android.app.Application
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.chsbuffer.revancedxposed.BaseHook
import io.github.chsbuffer.revancedxposed.googlephotos.misc.features.SpoofFeaturesPatch

val GooglePhotosPatches = arrayOf(SpoofFeaturesPatch)

class GooglePhotosHook(app: Application, lpparam: LoadPackageParam) : BaseHook(app, lpparam) {
    override val classLoader = lpparam.classLoader!!
    override val patches = GooglePhotosPatches
}