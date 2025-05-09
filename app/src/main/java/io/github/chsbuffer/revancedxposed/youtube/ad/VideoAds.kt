package io.github.chsbuffer.revancedxposed.youtube.ad

import de.robv.android.xposed.XC_MethodReplacement
import io.github.chsbuffer.revancedxposed.youtube.YoutubeHook


fun YoutubeHook.VideoAds() {
    getDexMethod("LoadVideoAds") {
        findMethod {
            matcher {
                usingEqStrings(
                    listOf(
                        "TriggerBundle doesn't have the required metadata specified by the trigger ",
                        "Tried to enter slot with no assigned slotAdapter",
                        "Trying to enter a slot when a slot of same type and physical position is already active. Its status: ",
                    )
                )
            }
        }.single()
    }.hookMethod(XC_MethodReplacement.DO_NOTHING)
}
