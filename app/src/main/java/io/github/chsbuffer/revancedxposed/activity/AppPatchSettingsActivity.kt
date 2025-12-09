@file:Suppress("DEPRECATION")

package io.github.chsbuffer.revancedxposed.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Vibrator
import android.preference.CheckBoxPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.Button
import io.github.chsbuffer.revancedxposed.R

class AppPatchSettingsActivity : Activity() {

    companion object {
        const val ARGUMENT_APP_NAME = "app_name_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_patch_settings)

        actionBar?.setDisplayHomeAsUpEnabled(true)

        val appName = intent.getStringExtra(ARGUMENT_APP_NAME)
        actionBar?.title = appName

        if (savedInstanceState != null) return
        val fragment = AppPatchSettingsFragment().apply {
            arguments = Bundle().apply {
                putString(ARGUMENT_APP_NAME, appName)
            }
        }
        fragmentManager.beginTransaction()
            .replace(R.id.app_patch_settings_container, fragment)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("WorldReadableFiles")
    class AppPatchSettingsFragment : PreferenceFragment() {

        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Retrieve appName from the Activity's Intent extras
            val appName = arguments?.getString(ARGUMENT_APP_NAME)
            val appPatchInfo = appPatchConfigurations.find { it.appName == appName }
            if (appPatchInfo == null) throw Exception("AppPatchInfo not found, app_name: $appName")

            val screen = preferenceManager.createPreferenceScreen(context)
            /** XSharedPreference
             * @see io.github.chsbuffer.revancedxposed.BaseHook.xpref */
            preferenceManager.sharedPreferencesMode = MODE_WORLD_READABLE
            preferenceManager.sharedPreferencesName = appPatchInfo.packageName

            object : Preference(context) {
                @Deprecated("Deprecated in Java")
                override fun onBindView(view: View) {
                    super.onBindView(view)
                    view.findViewById<Button>(R.id.button_all).setOnClickListener {
                        setAllPreferences(true)
                    }
                    view.findViewById<Button>(R.id.button_none).setOnClickListener {
                        setAllPreferences(false)
                    }
                    val isInstalled = runCatching {
                        context.packageManager.getPackageInfo(appPatchInfo.packageName, 0)
                    }.isSuccess

                    view.findViewById<Button>(R.id.button_app_info).apply {
                        if (!isInstalled) visibility = View.GONE
                        setOnClickListener {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.parse("package:${appPatchInfo.packageName}"))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }
                }
            }.apply {
                layoutResource = R.layout.preference_header_buttons
                screen.addPreference(this)
            }

            for (patchInfo in appPatchInfo.patches.sortedBy { it.name }) {
                if (patchInfo.name == "") continue
                if (patchInfo.name.startsWith("<")) continue
                CheckBoxPreference(context).apply {
                    /** XSharedPreference
                     * @see io.github.chsbuffer.revancedxposed.BaseHook.applyPatches */
                    key = patchInfo.name // Pref Key
                    title = patchInfo.name
                    summary = patchInfo.description
                    setDefaultValue(true)
                    setOnPreferenceChangeListener { _, _ ->
                        val vibrator =
                            context.getSystemService(VIBRATOR_SERVICE) as Vibrator?
                        if (vibrator?.hasVibrator() ?: false) {
                            vibrator.vibrate(50)
                        }
                        true
                    }
                    screen.addPreference(this)
                }
            }

            preferenceScreen = screen
        }

        fun setAllPreferences(enable: Boolean) {
            if (!isAdded) return
            for (i in 0 until preferenceScreen.preferenceCount) {
                val preference = preferenceScreen.getPreference(i)
                if (preference is CheckBoxPreference) {
                    preference.isChecked = enable
                }
            }
        }
    }
}
