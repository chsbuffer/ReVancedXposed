package io.github.chsbuffer.revancedxposed.youtube.video.speed.custom

import app.revanced.extension.youtube.patches.components.PlaybackSpeedMenuFilter
import app.revanced.extension.youtube.patches.playback.speed.CustomPlaybackSpeedPatch
import app.revanced.extension.youtube.patches.playback.speed.CustomPlaybackSpeedPatch.customPlaybackSpeeds
import io.github.chsbuffer.revancedxposed.invokeOriginalMethod
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.scopedHook
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.InputType
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.TextPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.litho.filter.LithoFilter
import io.github.chsbuffer.revancedxposed.youtube.misc.litho.filter.addLithoFilter
import io.github.chsbuffer.revancedxposed.youtube.misc.recyclerviewtree.hook.addRecyclerViewTreeHook
import io.github.chsbuffer.revancedxposed.youtube.misc.recyclerviewtree.hook.recyclerViewTreeHook
import io.github.chsbuffer.revancedxposed.youtube.video.information.doOverridePlaybackSpeed
import io.github.chsbuffer.revancedxposed.youtube.video.speed.settingsMenuVideoSpeedGroup
import java.lang.reflect.Method

private var INSTANCE: Any? = null
private lateinit var showOldPlaybackSpeedMenuMethod: Method
fun doShowOldPlaybackSpeedMenu() {
    if (INSTANCE != null) showOldPlaybackSpeedMenuMethod(INSTANCE)
}

val CustomPlaybackSpeed = patch(
    description = "Adds custom playback speed options.",
) {
    dependsOn(
        LithoFilter, recyclerViewTreeHook
    )

    settingsMenuVideoSpeedGroup.addAll(
        listOf(
            SwitchPreference("revanced_custom_speed_menu"),
            SwitchPreference("revanced_restore_old_speed_menu"),
            TextPreference(
                "revanced_custom_playback_speeds", inputType = InputType.TEXT_MULTI_LINE
            ),
        )
    )

    // Override the min/max speeds that can be used.
    ::speedLimiterFingerprint.hookMethod(scopedHook(::clampFloatFingerprint.member) {
        before {
            it.args[1] = 0.0f
            it.args[2] = 8.0f
        }
    })

    // Replace the speeds float array with custom speeds.
    // These speeds are used if the speed menu is immediately opened after a video is opened.
    ::speedArrayGeneratorFingerprint.hookMethod {
        val source = ::speedsFloatArrayField.field.get(null) as FloatArray
        val chunkSize = source.size
        before {
            /*
            * The method hardcoded array length (determined during compilation/obfuscation)
            * to iterate through the playback speed float values in PlayerConfigModel.
            * To bypass this constraint,
            * We divide the custom speeds into chunks matching the original array's size,
            * repeatedly populate the original static float array,
            * Invoke the original method for each chunk to transform raw floats into the expected Model objects.
            * */
            val result = customPlaybackSpeeds.asIterable().chunked(chunkSize).map { chunk ->
                chunk.forEachIndexed { index, value -> source[index] = value }
                (it.invokeOriginalMethod() as Array<*>)
            }.flatMap { it.asIterable() }

            val arr = java.lang.reflect.Array.newInstance(result.first()!!.javaClass, result.size)
            result.forEachIndexed { i, v -> java.lang.reflect.Array.set(arr, i, v) }

            it.result = arr
        }
    }

    // region Force old video quality menu.

    ::getOldPlaybackSpeedsFingerprint.hookMethod {
        before {
            INSTANCE = it.thisObject
        }
    }
    showOldPlaybackSpeedMenuMethod = ::showOldPlaybackSpeedMenuFingerprint.method

    // endregion

    // Close the unpatched playback dialog and show the modern custom dialog.
    addRecyclerViewTreeHook.add { CustomPlaybackSpeedPatch.onFlyoutMenuCreate(it) }

    // Required to check if the playback speed menu is currently shown.
    addLithoFilter(PlaybackSpeedMenuFilter())

    // region Custom tap and hold 2x speed.

    runCatching {
        val tapAndHoldPath = ThreadLocal<Boolean>()
        ::onSpeedTapAndHoldFingerprint.hookMethod {
            before { tapAndHoldPath.remove() }
            after {
                if (tapAndHoldPath.get() == true) {
                    doOverridePlaybackSpeed(CustomPlaybackSpeedPatch.tapAndHoldSpeed())
                }
            }
        }
        ::onSpeedTapAndHoldFingerprint.hookMethod(
            scopedHook(::getPlaybackSpeedMethodReference.member) {
                before {
                    tapAndHoldPath.set(true)
                }
            })

        settingsMenuVideoSpeedGroup.add(
            TextPreference("revanced_speed_tap_and_hold", inputType = InputType.NUMBER_DECIMAL),
        )
    }

    // endregion
}

