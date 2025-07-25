@file:Suppress("DEPRECATION", "DiscouragedApi")

package io.github.chsbuffer.revancedxposed.shared.misc.settings.preference

import android.content.Context
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceManager
import app.revanced.extension.shared.Utils
import app.revanced.extension.shared.settings.preference.CustomDialogListPreference

class ListPreference(
    key: String? = null,
    titleKey: String = "${key}_title",
    summaryKey: String? = null,
    icon: String? = null,
    layout: String? = null,
    tag: Class<out ListPreference> = CustomDialogListPreference::class.java,
    val entriesKey: String? = "${key}_entries",
    val entryValuesKey: String? = "${key}_entry_values"
) : BasePreference(key, titleKey, summaryKey, icon, layout, tag) {

    override fun build(ctx: Context, prefMgr: PreferenceManager): Preference {
        return super.build(ctx, prefMgr).apply {
            val listPreference = this as ListPreference
            entriesKey?.let {
                listPreference.setEntries(Utils.getResourceIdentifier(entriesKey, "array"))
            }
            entryValuesKey?.let {
                listPreference.setEntryValues(Utils.getResourceIdentifier(entryValuesKey, "array"))
            }
        }
    }
}