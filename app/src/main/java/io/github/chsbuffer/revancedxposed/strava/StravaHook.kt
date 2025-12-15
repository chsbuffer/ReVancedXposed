package io.github.chsbuffer.revancedxposed.strava

import io.github.chsbuffer.revancedxposed.strava.subscription.UnlockSubscription
import io.github.chsbuffer.revancedxposed.strava.upselling.DisableSubscriptionSuggestions

val StravaPatches = arrayOf(
    UnlockSubscription,
    DisableSubscriptionSuggestions
)