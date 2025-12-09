package io.github.chsbuffer.revancedxposed.photomath

import android.app.Application
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chsbuffer.revancedxposed.BaseHook
import io.github.chsbuffer.revancedxposed.photomath.misc.unlock.plus.UnlockPlus

class PhotomathHook(app: Application, lpparam: XC_LoadPackage.LoadPackageParam) : BaseHook(
    app, lpparam
) {
    override val patches = arrayOf(UnlockPlus)
}