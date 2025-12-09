package io.github.chsbuffer.revancedxposed.youtube.ad.general

import android.view.View
import app.revanced.extension.shared.Logger
import app.revanced.extension.shared.Utils
import app.revanced.extension.youtube.patches.components.AdsFilter
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.litho.filter.LithoFilter
import io.github.chsbuffer.revancedxposed.youtube.misc.litho.filter.addLithoFilter
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen
import io.github.chsbuffer.revancedxposed.youtube.misc.verticalscroll.FixVerticalScroll

val HideAds = patch(
    name = "Hide ads",
    description = "Adds options to remove general ads.",
) {
    dependsOn(
        FixVerticalScroll,
        LithoFilter
    )

    PreferenceScreen.ADS.addPreferences(
        SwitchPreference("revanced_hide_creator_store_shelf"),
//        SwitchPreference("revanced_hide_end_screen_store_banner"),
        SwitchPreference("revanced_hide_fullscreen_ads"),
        SwitchPreference("revanced_hide_general_ads"),
        SwitchPreference("revanced_hide_merchandise_banners"),
        SwitchPreference("revanced_hide_paid_promotion_label"),
        SwitchPreference("revanced_hide_self_sponsor_ads"),
        SwitchPreference("revanced_hide_shopping_links"),
        SwitchPreference("revanced_hide_view_products_banner"),
        SwitchPreference("revanced_hide_web_search_results"),
    )

    addLithoFilter(AdsFilter())

    // TODO: Hide end screen store banner

    // Hide ad views
    val adAttributionId = Utils.getResourceIdentifier("ad_attribution", "id")

    XposedHelpers.findAndHookMethod(
        View::class.java.name,
        lpparam.classLoader,
        "findViewById",
        Int::class.java.name,
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.args[0] == adAttributionId) {
                    Logger.printDebug { "Hide Ad Attribution View" }
                    AdsFilter.hideAdAttributionView(param.result as View)
                }
            }
        })
}