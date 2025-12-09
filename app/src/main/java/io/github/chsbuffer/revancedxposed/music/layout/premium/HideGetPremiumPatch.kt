package io.github.chsbuffer.revancedxposed.music.layout.premium

import android.view.View
import app.revanced.extension.music.patches.HideGetPremiumPatch
import app.revanced.extension.shared.Logger
import app.revanced.extension.shared.Utils
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.music.misc.settings.PreferenceScreen
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference

val HideGetPremium = patch(
    name = "Hide 'Get Music Premium'",
    description = "Adds an option to hide the \"Get Music Premium\" label in the settings and account menu.",
) {
    PreferenceScreen.ADS.addPreferences(
        SwitchPreference("revanced_music_hide_get_premium_label"),
    )

    ::hideGetPremiumFingerprint.hookMethod {
        val id = Utils.getResourceIdentifier("unlimited_panel", "id")
        after { param ->
            val thiz = param.thisObject
            for (field in thiz.javaClass.fields) {
                val view = field.get(thiz)
                if (view !is View) continue
                val panelView = view.findViewById<View>(id) ?: continue
                Logger.printDebug { "hide get premium" }
                panelView.visibility = View.GONE
                break
            }
        }
    }

    ::membershipSettingsFingerprint.hookMethod {
        before {
            if (HideGetPremiumPatch.hideGetPremiumLabel()) it.result = null
        }
    }
}