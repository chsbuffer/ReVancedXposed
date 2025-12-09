package io.github.chsbuffer.revancedxposed.music.misc.settings

import android.app.Activity
import app.revanced.extension.music.settings.MusicActivityHook
import app.revanced.extension.shared.Logger
import app.revanced.extension.shared.Utils
import app.revanced.extension.shared.settings.preference.ImportExportPreference
import app.revanced.extension.shared.settings.preference.ReVancedAboutPreference
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import io.github.chsbuffer.revancedxposed.R
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.hookMethod
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.BasePreferenceScreen
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.InputType
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.NonInteractivePreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.PreferenceScreenPreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.TextPreference
import io.github.chsbuffer.revancedxposed.shared.settings.preferences
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceFragmentCompat_addPreferencesFromResource

val SettingsHook = patch(
    description = "Adds settings for ReVanced to YouTube Music.",
) {
    val addPreferencesFromResource = ::PreferenceFragmentCompat_addPreferencesFromResource.method

    ::PreferenceFragmentCompat_setPreferencesFromResource.hookMethod {
        before {
            val context = Utils.getContext()
            val preferencesName = context.resources.getResourceName(it.args[0] as Int)
            Logger.printDebug { "setPreferencesFromResource $preferencesName" }
            if (!preferencesName.endsWith("settings_headers")) return@before
            addPreferencesFromResource(it.thisObject, R.xml.yt_revanced_settings_music)
            addPreferencesFromResource(it.thisObject, it.args[0])
            it.result = Unit
        }
    }

    // Add an "About" preference to the top.
    preferences += NonInteractivePreference(
        key = "revanced_settings_music_screen_0_about",
        summaryKey = null,
        tag = ReVancedAboutPreference::class.java,
        selectable = true,
    )

    PreferenceScreen.GENERAL.addPreferences(
        SwitchPreference("revanced_settings_search_history")
    )

    PreferenceScreen.MISC.addPreferences(
        TextPreference(
            key = null,
            titleKey = "revanced_pref_import_export_title",
            summaryKey = "revanced_pref_import_export_summary",
            inputType = InputType.TEXT_MULTI_LINE,
            tag = ImportExportPreference::class.java,
        )
    )

    val superOnCreate =
        Activity::class.java.getDeclaredMethod("onCreate", android.os.Bundle::class.java)
    superOnCreate.hookMethod { }
    ::googleApiActivityFingerprint.hookMethod {
        before { param ->
            val activity = param.thisObject as Activity
            activity.setTheme(Utils.getResourceIdentifier("@style/Theme.YouTubeMusic", "style"))
            MusicActivityHook.initialize(activity)
            activity.theme.applyStyle(R.style.ListDividerNull, true)
            XposedBridge.invokeOriginalMethod(superOnCreate, param.thisObject, param.args)
            param.result = Unit
        }
    }

    // Remove other methods as they will break as the onCreate method is modified above.
    ::googleApiActivityNOTonCreate.dexMethodList.forEach {
        if (it.returnTypeName == "void") it.hookMethod(XC_MethodReplacement.DO_NOTHING)
    }

    PreferenceScreen.close()
}

object PreferenceScreen : BasePreferenceScreen() {
    val ADS = Screen(
        "revanced_settings_music_screen_1_ads", summaryKey = null
    )
    val GENERAL = Screen(
        "revanced_settings_music_screen_2_general", summaryKey = null
    )
    val PLAYER = Screen(
        "revanced_settings_music_screen_3_player", summaryKey = null
    )
    val MISC = Screen(
        "revanced_settings_music_screen_4_misc", summaryKey = null
    )

    override fun commit(screen: PreferenceScreenPreference) {
        preferences += screen
    }
}
