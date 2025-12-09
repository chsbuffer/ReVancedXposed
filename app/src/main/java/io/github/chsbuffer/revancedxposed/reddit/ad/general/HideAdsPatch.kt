package io.github.chsbuffer.revancedxposed.reddit.ad.general

import app.revanced.extension.shared.Logger
import io.github.chsbuffer.revancedxposed.findFieldByExactType
import io.github.chsbuffer.revancedxposed.getObjectField
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.reddit.ad.banner.HideBanner
import io.github.chsbuffer.revancedxposed.reddit.ad.comments.HideCommentAds
import io.github.chsbuffer.revancedxposed.setObjectField

val HideAds = patch(
    name = "Hide ads",) {
    dependsOn(
        HideBanner, HideCommentAds
    )
    // region Filter promoted ads (does not work in popular or latest feed)
    ::adPostFingerprint.hookMethod {
        val iLink = classLoader.loadClass("com.reddit.domain.model.ILink")
        val getPromoted = iLink.methods.single { it.name == "getPromoted" }
        after { param ->
            val arrayList = param.thisObject.getObjectField("children") as Iterable<Any?>
            val result = mutableListOf<Any?>()
            var filtered = 0
            for (item in arrayList) {
                try {
                    if (item != null && iLink.isAssignableFrom(item.javaClass) && getPromoted(item) == true) {
                        filtered++
                        continue
                    }
                } catch (_: Throwable) {
                    Logger.printDebug { "not iLink, keep it" }
                    // not iLink, keep it
                }
                result.add(item)
            }
            Logger.printDebug { "Filtered $filtered ads in ${arrayList.count()} posts" }
            param.thisObject.setObjectField("children", result)
        }
    }

    // endregion

    // region Remove ads from popular and latest feed

    // The new feeds work by inserting posts into lists.
    // AdElementConverter is conveniently responsible for inserting all feed ads.
    // By removing the appending instruction no ad posts gets appended to the feed.
    ::AdPostSectionInitFingerprint.hookMethod {
        before { param ->
            val sections = param.args[3] as MutableList<*>
            sections.javaClass.findFieldByExactType(Array<Any>::class.java)!!
                .set(sections, emptyArray<Any>())
            Logger.printDebug { "Removed ads from popular and latest feed" }
        }
    }

    // endregion
}