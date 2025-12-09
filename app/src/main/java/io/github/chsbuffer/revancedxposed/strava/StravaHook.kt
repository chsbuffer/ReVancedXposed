package io.github.chsbuffer.revancedxposed.strava

import android.app.Application
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chsbuffer.revancedxposed.BaseHook
import io.github.chsbuffer.revancedxposed.strava.subscription.UnlockSubscription
import io.github.chsbuffer.revancedxposed.strava.upselling.DisableSubscriptionSuggestions

val StravaPatches = arrayOf(
    UnlockSubscription,
    DisableSubscriptionSuggestions
)

class StravaHook(app: Application, lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook(app, lpparam) {
    override val patches = StravaPatches
}

