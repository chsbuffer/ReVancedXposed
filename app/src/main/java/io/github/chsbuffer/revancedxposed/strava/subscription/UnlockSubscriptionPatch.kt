package io.github.chsbuffer.revancedxposed.strava.subscription

import io.github.chsbuffer.revancedxposed.patch

val UnlockSubscription = patch(
    name = "Unlock subscription features",
    description = "Unlocks \"Routes\", \"Matched Runs\" and \"Segment Efforts\".",
) {
    ::getSubscribedFingerprint.hookMethod {
        before { param ->
            param.result = true
        }
    }
}