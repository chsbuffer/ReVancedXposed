package io.github.chsbuffer.revancedxposed.youtube.interaction.swipecontrols

import app.revanced.extension.shared.settings.preference.ColorPickerWithOpacitySliderPreference
import app.revanced.extension.youtube.swipecontrols.SwipeControlsHostActivity
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.InputType
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.ListPreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.TextPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.playertype.PlayerTypeHook
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen
import io.github.chsbuffer.revancedxposed.youtube.shared.mainActivityClass

val SwipeControls = patch(
    name = "Swipe controls",
    description = "Adds options to enable and configure volume and brightness swipe controls.",
) {
    dependsOn(
        PlayerTypeHook,
    )

//    if (is_19_43_or_greater) {
//        PreferenceScreen.SWIPE_CONTROLS.addPreferences(
//            SwitchPreference("revanced_swipe_change_video")
//        )
//    }

    PreferenceScreen.SWIPE_CONTROLS.addPreferences(
        SwitchPreference("revanced_swipe_brightness"),
        SwitchPreference("revanced_swipe_volume"),
        SwitchPreference("revanced_swipe_press_to_engage"),
        SwitchPreference("revanced_swipe_haptic_feedback"),
        SwitchPreference("revanced_swipe_save_and_restore_brightness"),
        SwitchPreference("revanced_swipe_lowest_value_enable_auto_brightness"),
        ListPreference("revanced_swipe_overlay_style"),
        TextPreference("revanced_swipe_overlay_background_opacity", inputType = InputType.NUMBER),
        TextPreference(
            "revanced_swipe_overlay_progress_brightness_color",
            tag = ColorPickerWithOpacitySliderPreference::class.java,
            inputType = InputType.TEXT_CAP_CHARACTERS
        ),
        TextPreference(
            "revanced_swipe_overlay_progress_volume_color",
            tag = ColorPickerWithOpacitySliderPreference::class.java,
            inputType = InputType.TEXT_CAP_CHARACTERS
        ),
        TextPreference("revanced_swipe_text_overlay_size", inputType = InputType.NUMBER),
        TextPreference("revanced_swipe_overlay_timeout", inputType = InputType.NUMBER),
        TextPreference("revanced_swipe_threshold", inputType = InputType.NUMBER),
        TextPreference("revanced_swipe_volume_sensitivity", inputType = InputType.NUMBER),
    )

    SwipeControlsHostActivity.hookActivity(::mainActivityClass.clazz)

    // TODO patch to enable/disable swipe to change video.
}