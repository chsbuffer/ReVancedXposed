package io.github.chsbuffer.revancedxposed.shared.misc.privacy

import android.content.ClipData
import android.content.Intent
import app.revanced.extension.shared.patches.SanitizeSharingLinksPatch
import app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import io.github.chsbuffer.revancedxposed.PatchExecutor
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.BasePreferenceScreen
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.PreferenceCategory
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference

fun PatchExecutor.SanitizeSharingLinks(
    preferenceScreen: BasePreferenceScreen.Screen,
    replaceMusicLinksWithYouTube: Boolean = false
) {

    val sanitizePreference = SwitchPreference("revanced_sanitize_sharing_links")

    preferenceScreen.addPreferences(
        if (replaceMusicLinksWithYouTube) {
            PreferenceCategory(
                titleKey = null,
                sorting = Sorting.UNSORTED,
                tag = NoTitlePreferenceCategory::class.java,
                preferences = setOf(
                    sanitizePreference,
                    SwitchPreference("revanced_replace_music_with_youtube")
                )
            )
        } else {
            sanitizePreference
        }
    )

    val sanitizeArg1 = object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            val url = param.args[1] as? String ?: return
            if (!url.startsWith("https://")) return
            param.args[1] = SanitizeSharingLinksPatch.sanitize(url)
        }
    }

    XposedHelpers.findAndHookMethod(
        ClipData::class.java.name,
        lpparam.classLoader,
        "newPlainText",
        CharSequence::class.java,
        CharSequence::class.java,
        sanitizeArg1
    )

    XposedHelpers.findAndHookMethod(
        Intent::class.java.name,
        lpparam.classLoader,
        "putExtra",
        String::class.java,
        String::class.java,
        sanitizeArg1
    )
}