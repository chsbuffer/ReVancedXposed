package io.github.chsbuffer.revancedxposed.youtube.misc.verticalscroll

import de.robv.android.xposed.XC_MethodReplacement.returnConstant
import io.github.chsbuffer.revancedxposed.patch

val FixVerticalScroll = patch(
    description = "Fixes issues with refreshing the feed when the first component is of type EmptyComponent."
) {
    ::canScrollVerticallyFingerprint.hookMethod(returnConstant(false))
}