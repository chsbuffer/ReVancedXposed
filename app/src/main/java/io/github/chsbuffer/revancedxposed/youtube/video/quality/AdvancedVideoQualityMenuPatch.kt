package io.github.chsbuffer.revancedxposed.youtube.video.quality

import android.view.View
import android.widget.ListView
import app.revanced.extension.shared.Utils
import app.revanced.extension.youtube.patches.components.AdvancedVideoQualityMenuFilter
import app.revanced.extension.youtube.patches.playback.quality.AdvancedVideoQualityMenuPatch
import io.github.chsbuffer.revancedxposed.findFirstFieldByExactType
import io.github.chsbuffer.revancedxposed.scopedHook
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.youtube.YoutubeHook
import io.github.chsbuffer.revancedxposed.youtube.misc.litho.filter.LithoFilter
import io.github.chsbuffer.revancedxposed.youtube.misc.litho.filter.addLithoFilter
import io.github.chsbuffer.revancedxposed.youtube.misc.recyclerviewtree.hook.addRecyclerViewTreeHook
import io.github.chsbuffer.revancedxposed.youtube.misc.recyclerviewtree.hook.recyclerViewTreeHook
import org.luckypray.dexkit.wrap.DexMethod
import java.lang.reflect.Field

fun YoutubeHook.AdvancedVideoQualityMenu() {
    dependsOn(
        ::LithoFilter,
        ::recyclerViewTreeHook,
    )

    settingsMenuVideoQualityGroup.add(
        SwitchPreference("revanced_advanced_video_quality_menu")
    )

    // region Patch for the old type of the video quality menu.
    // Used for regular videos when spoofing to old app version,
    // and for the Shorts quality flyout on newer app versions.
    ::videoQualityMenuViewInflateFingerprint.dexMethodList.forEach {
        it.hookMethod(scopedHook(DexMethod("Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;").toMember()) {
            val bottom_sheet_list_fragment =
                Utils.getResourceIdentifier("bottom_sheet_list_fragment", "layout")
            val bottom_sheet_list_view = Utils.getResourceIdentifier("bottom_sheet_list_view", "id")
            after {
                if (it.args[0] != bottom_sheet_list_fragment) return@after
                val view = it.result as View
                val listView: ListView = view.findViewById(bottom_sheet_list_view)
                AdvancedVideoQualityMenuPatch.addVideoQualityListMenuListener(listView)
            }
        })
    }

    // Force YT to add the 'advanced' quality menu for Shorts.
    ::videoQualityMenuOptionsFingerprint.hookMethod {
        var useQualityMenu: Boolean? = null
        var field: Field? = null
        before {
            val p3 = it.args[2]
            if (p3 == null) return@before
            field = field ?: p3.javaClass.findFirstFieldByExactType(Boolean::class.java)
            useQualityMenu = field.get(p3) as Boolean
            field.set(
                p3,
                AdvancedVideoQualityMenuPatch.forceAdvancedVideoQualityMenuCreation(useQualityMenu)
            )
        }
        after {
            val p2 = it.args[2]
            if (p2 == null) return@after
            field!!.set(p2, useQualityMenu)
        }
    }

    // region Patch for the new type of the video quality menu.
    addRecyclerViewTreeHook.add(AdvancedVideoQualityMenuPatch::onFlyoutMenuCreate)
    // Required to check if the video quality menu is currently shown in order to click on the "Advanced" item.
    addLithoFilter(AdvancedVideoQualityMenuFilter())
    // endregion
}