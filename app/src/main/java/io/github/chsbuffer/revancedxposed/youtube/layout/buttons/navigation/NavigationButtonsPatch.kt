package io.github.chsbuffer.revancedxposed.youtube.layout.buttons.navigation

import android.widget.TextView
import app.revanced.extension.youtube.patches.NavigationButtonsPatch
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.scopedHook
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.PreferenceScreenPreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.navigation.NavigationBarHook
import io.github.chsbuffer.revancedxposed.youtube.misc.navigation.hookNavigationButtonCreated
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen
import org.luckypray.dexkit.wrap.DexMethod

val NavigationButtons = patch(
    name = "Navigation buttons",
    description = "Adds options to hide and change navigation buttons (such as the Shorts button).",
) {
    dependsOn(NavigationBarHook)

    val preferences = mutableSetOf(
        SwitchPreference("revanced_hide_home_button"),
        SwitchPreference("revanced_hide_shorts_button"),
        SwitchPreference("revanced_hide_create_button"),
        SwitchPreference("revanced_hide_subscriptions_button"),
        SwitchPreference("revanced_hide_notifications_button"),
        SwitchPreference("revanced_switch_create_with_notifications_button"),
        SwitchPreference("revanced_hide_navigation_button_labels"),
    )

//    if (is_19_25_or_greater) {
//        preferences += SwitchPreference("revanced_disable_translucent_navigation_bar_light")
//        preferences += SwitchPreference("revanced_disable_translucent_navigation_bar_dark")
//
//        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
//            SwitchPreference("revanced_disable_translucent_status_bar")
//        )
//    }

    PreferenceScreen.GENERAL_LAYOUT.addPreferences(
        PreferenceScreenPreference(
            key = "revanced_navigation_buttons_screen",
            sorting = PreferenceScreenPreference.Sorting.UNSORTED,
            preferences = preferences
        )
    )

    // Switch create with notifications button.
    ::addCreateButtonViewFingerprint.hookMethod(scopedHook(::AutoMotiveFeatureMethod.member) {
        before { param ->
            param.result = NavigationButtonsPatch.switchCreateWithNotificationButton()
        }
    })

    // Hide navigation button labels.
    ::createPivotBarFingerprint.hookMethod(scopedHook(DexMethod("Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V").toMethod()) {
        before { param ->
            NavigationButtonsPatch.hideNavigationButtonLabels(param.thisObject as TextView)
        }
    })

    // Hook navigation button created, in order to hide them.
    hookNavigationButtonCreated.add { button, view ->
        NavigationButtonsPatch.navigationTabCreated(
            button,
            view
        )
    }

    // TODO Force on/off translucent effect on status bar and navigation buttons.
}