@file:Suppress("DEPRECATION") @file:SuppressLint("WorldReadableFiles")

package io.github.chsbuffer.revancedxposed.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.view.MenuItem
import app.revanced.extension.shared.Utils
import app.revanced.extension.shared.settings.preference.ReVancedAboutPreference
import io.github.chsbuffer.revancedxposed.AppPatchInfo
import io.github.chsbuffer.revancedxposed.R
import io.github.chsbuffer.revancedxposed.appPatchConfigurations
import io.github.chsbuffer.revancedxposed.common.UpdateChecker

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        actionBar?.setDisplayShowHomeEnabled(true)
        if (savedInstanceState != null) return

        fragmentManager.beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragment() {
        fun AppPatchInfo.getPreference(): Preference {
            val preference = Preference(context)
            preference.title = appName
            preference.key = appName
            preference.intent = Intent(context, AppPatchSettingsActivity::class.java).apply {
                putExtra(AppPatchSettingsActivity.ARGUMENT_APP_NAME, appName)
            }
            return preference
        }

        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val rootScreen = preferenceManager.createPreferenceScreen(context)
            preferenceScreen = rootScreen

            Utils.setContext(context)
            ReVancedAboutPreference(context).apply {
                setTitle(R.string.about_title)
                rootScreen.addPreference(this)
            }

            addPreferencesFromResource(R.xml.license_prefs)

            Preference(context).apply {
                setTitle(R.string.check_for_update_title)
                setOnPreferenceClickListener {
                    UpdateChecker().apply {
                        setActivity(activity)
                        checkUpdate(silent = false)
                    }
                    true
                }
                rootScreen.addPreference(this)
            }

            val isModuleActivated: Boolean = try {
                context.getSharedPreferences("prefs", MODE_WORLD_READABLE)
                true
            } catch (_: SecurityException) {
                false
            }

            if (!isModuleActivated) {
                rootScreen.addPreference(Preference(context).apply {
                    summary = "Activate this module in Module Management!"
                    isEnabled = false
                })
                return
            }

            val patchSelectionCategory = PreferenceCategory(context).apply {
                setTitle(R.string.patch_selection)
                rootScreen.addPreference(this)
            }
            Preference(context).apply {
                setSummary(R.string.force_stop_to_apply_summary)
                isEnabled = false
                patchSelectionCategory.addPreference(this)
            }

            for (appPatchInfo in appPatchConfigurations) {
                patchSelectionCategory.addPreference(appPatchInfo.getPreference())
            }
        }
    }
}
