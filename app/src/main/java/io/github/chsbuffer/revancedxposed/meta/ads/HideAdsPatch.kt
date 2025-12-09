package io.github.chsbuffer.revancedxposed.meta.ads

import de.robv.android.xposed.XC_MethodReplacement
import io.github.chsbuffer.revancedxposed.patch

val HideAds = patch(
    name = "Hide ads",
) {
    ::adInjectorFingerprint.hookMethod(XC_MethodReplacement.DO_NOTHING)
}