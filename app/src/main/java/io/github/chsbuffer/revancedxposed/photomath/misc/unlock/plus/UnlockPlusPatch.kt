package io.github.chsbuffer.revancedxposed.photomath.misc.unlock.plus

import de.robv.android.xposed.XC_MethodReplacement
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.photomath.misc.unlock.bookpoint.EnableBookpoint

val UnlockPlus = patch(
    name = "Unlock plus",
) {
    dependsOn(EnableBookpoint)
    ::isPlusUnlockedFingerprint.hookMethod(XC_MethodReplacement.returnConstant(true))
}