package io.github.chsbuffer.revancedxposed.twitch

import android.app.Application
import app.revanced.extension.twitch.patches.VideoAdsPatch
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.chsbuffer.revancedxposed.BaseHook
import io.github.chsbuffer.revancedxposed.fingerprint
import io.github.chsbuffer.revancedxposed.getStaticObjectField
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.twitch.misc.PreferenceScreen
import org.luckypray.dexkit.query.enums.StringMatchType
import org.luckypray.dexkit.wrap.DexClass
import org.luckypray.dexkit.wrap.DexMethod

class TwitchHook(app: Application, lpparam: LoadPackageParam) : BaseHook(app, lpparam) {
    override val hooks = arrayOf(::ExtensionHook, ::VideoAds)
    fun ExtensionHook() {

    }

    class ReturnMethod(val returnType: Char, val value: String) {
        companion object {
            val default = ReturnMethod('V', "")
        }
    }

    fun VideoAds() {
        PreferenceScreen.ADS.CLIENT_SIDE.addPreferences(
            SwitchPreference("revanced_block_video_ads"),
        )

        fun blockMethods(
            clazz: String, methodNames: Set<String>, returnMethod: ReturnMethod
        ) {
            val clazz = runCatching { DexClass(clazz).toClass() }.getOrNull() ?: return
            clazz.methods.filter { it.name in methodNames }.forEach {
                it.hookMethod(object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!VideoAdsPatch.shouldBlockVideoAds()) return

                        param.result = when (returnMethod.returnType) {
                            'V' -> Unit
                            'Z' -> returnMethod.value != "0"
                            else -> throw NotImplementedError()
                        }
                    }
                })
            }
        }

        /* Amazon ads SDK */
        blockMethods(
            "Lcom/amazon/ads/video/player/AdsManagerImpl;",
            setOf("playAds"),
            ReturnMethod.default,
        )

        /* Twitch ads manager */
        blockMethods(
            "Ltv/twitch/android/shared/ads/VideoAdManager;",
            setOf(
                "checkAdEligibilityAndRequestAd",
                "requestAd",
                "requestAds",
            ),
            ReturnMethod.default,
        )

        /* Various ad presenters */
        blockMethods(
            "Ltv/twitch/android/shared/ads/AdsPlayerPresenter;",
            setOf(
                "requestAd",
                "requestFirstAd",
                "requestFirstAdIfEligible",
                "requestMidroll",
                "requestAdFromMultiAdFormatEvent",
            ),
            ReturnMethod.default,
        )

        blockMethods(
            "Ltv/twitch/android/shared/ads/AdsVodPlayerPresenter;",
            setOf(
                "requestAd",
                "requestFirstAd",
            ),
            ReturnMethod.default,
        )

        blockMethods(
            "Ltv/twitch/android/feature/theatre/ads/AdEdgeAllocationPresenter;",
            setOf(
                "parseAdAndCheckEligibility",
                "requestAdsAfterEligibilityCheck",
                "showAd",
                "bindMultiAdFormatAllocation",
            ),
            ReturnMethod.default,
        )

        /* A/B ad testing experiments */
        blockMethods(
            "Ltv/twitch/android/provider/experiments/helpers/DisplayAdsExperimentHelper;",
            setOf("areDisplayAdsEnabled"),
            ReturnMethod('Z', "0"),
        )

        blockMethods(
            "Ltv/twitch/android/shared/ads/tracking/MultiFormatAdsTrackingExperiment;",
            setOf(
                "shouldUseMultiAdFormatTracker",
                "shouldUseVideoAdTracker",
            ),
            ReturnMethod('Z', "0"),
        )

        blockMethods(
            "Ltv/twitch/android/shared/ads/MultiformatAdsExperiment;",
            setOf(
                "shouldDisableClientSideLivePreroll",
                "shouldDisableClientSideVodPreroll",
            ),
            ReturnMethod('Z', "1"),
        )

        // Pretend our player is ineligible for all ads.
        getDexMethod("checkAdEligibilityLambdaFingerprint") {
            fingerprint {
                returns("Lio/reactivex/Single;")
                parameters("L")
                classMatcher { className(".AdEligibilityFetcher", StringMatchType.EndsWith) }
                methodMatcher { name = "shouldRequestAd" }
            }
        }.hookMethod(object : XC_MethodHook() {
            val just =
                DexMethod("Lio/reactivex/Single;->just(Ljava/lang/Object;)Lio/reactivex/Single;").toMethod()

            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!VideoAdsPatch.shouldBlockVideoAds()) return
                param.result = just(null)
            }
        })

        getDexMethod("getReadyToShowAdFingerprint") {
            fingerprint {
                returns("Ltv/twitch/android/core/mvp/presenter/StateAndAction;")
                parameters("L", "L")
                classMatcher { className(".StreamDisplayAdsPresenter", StringMatchType.EndsWith) }
                methodMatcher { name = "getReadyToShowAdOrAbort" }
            }
        }.hookMethod(object : XC_MethodHook() {
            val adFormatDeclined =
                DexClass("Ltv/twitch/android/shared/display/ads/theatre/StreamDisplayAdsPresenter\$Action\$AdFormatDeclined;").toClass()

            val plus =
                DexMethod("Ltv/twitch/android/core/mvp/presenter/StateMachineKt;->plus(Ltv/twitch/android/core/mvp/presenter/PresenterState;Ltv/twitch/android/core/mvp/presenter/PresenterAction;)Ltv/twitch/android/core/mvp/presenter/StateAndAction;").toMethod()

            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!VideoAdsPatch.shouldBlockVideoAds()) return
                val presenterState = param.args[0]
                val presenterAction = adFormatDeclined.getStaticObjectField("INSTANCE")
                param.result = plus.invoke(presenterState, presenterAction)
            }
        })
    }
}