package io.github.chsbuffer.revancedxposed.youtube.ad.video

import de.robv.android.xposed.XC_MethodReplacement
import io.github.chsbuffer.revancedxposed.patch

val VideoAds = patch(
    name = "Video ads",
    description = "Adds an option to remove ads in the video player.",
) {
    ::loadVideoAdsFingerprint.hookMethod(XC_MethodReplacement.DO_NOTHING)
}