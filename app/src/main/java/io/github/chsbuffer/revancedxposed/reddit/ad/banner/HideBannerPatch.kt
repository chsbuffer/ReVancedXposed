package io.github.chsbuffer.revancedxposed.reddit.ad.banner

import android.view.View
import app.revanced.extension.shared.Logger
import app.revanced.extension.shared.Utils
import io.github.chsbuffer.revancedxposed.patch
import org.luckypray.dexkit.wrap.DexMethod

val HideBanner = patch(
    description = "Hides banner ads from comments on subreddits.",
) {
    val merge_listheader_link_detail =
        Utils.getResourceIdentifier("merge_listheader_link_detail", "layout")
    val ad_view_stub = Utils.getResourceIdentifier("ad_view_stub", "id")
    DexMethod("Landroid/view/View;->inflate(Landroid/content/Context;ILandroid/view/ViewGroup;)Landroid/view/View;").hookMethod {
        after { param ->
            val id = param.args[1] as Int
            if (id == merge_listheader_link_detail) {
                val view = param.result as View
                val stub = view.findViewById<View>(ad_view_stub)
                stub.layoutParams.apply {
                    width = 0
                    height = 0
                }

                Logger.printDebug { "Hide banner" }
            }
        }
    }
}