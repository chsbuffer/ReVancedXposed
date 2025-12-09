package io.github.chsbuffer.revancedxposed.photomath.misc.unlock.bookpoint

import de.robv.android.xposed.XC_MethodReplacement
import io.github.chsbuffer.revancedxposed.patch

val EnableBookpoint = patch(
    description = "Enables textbook access",
) {
    ::isBookpointEnabledFingerprint.hookMethod(XC_MethodReplacement.returnConstant(true))
}