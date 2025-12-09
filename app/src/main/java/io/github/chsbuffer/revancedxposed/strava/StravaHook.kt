package io.github.chsbuffer.revancedxposed.strava

import android.app.Application
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chsbuffer.revancedxposed.BaseHook
import io.github.chsbuffer.revancedxposed.strava.subscription.UnlockSubscription
import io.github.chsbuffer.revancedxposed.strava.upselling.DisableSubscriptionSuggestions

class StravaHook(app: Application, lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook(app, lpparam) {
    override val patches = arrayOf(
        UnlockSubscription,
        DisableSubscriptionSuggestions
    )
}

