package io.github.chsbuffer.revancedxposed.strava

import android.app.Application
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.chsbuffer.revancedxposed.BaseHook
import io.github.chsbuffer.revancedxposed.Opcode
import io.github.chsbuffer.revancedxposed.getObjectFieldOrNullAs
import io.github.chsbuffer.revancedxposed.opcodes
import org.luckypray.dexkit.query.enums.StringMatchType
import java.util.Collections

class StravaHook(app: Application, lpparam: XC_LoadPackage.LoadPackageParam) :
    BaseHook(app, lpparam) {
    override val hooks = arrayOf(
        ::UnlockSubscription,
        ::DisableSubscriptionSuggestions
    )

    fun UnlockSubscription() {
        getDexMethod("getSubscribedFingerprint") {
            findMethod {
                matcher {
                    name = "getSubscribed"
                    declaredClass(".SubscriptionDetailResponse", StringMatchType.EndsWith)
                    opcodes(
                        Opcode.IGET_BOOLEAN,
                    )
                }
            }.single()
        }.hookMethod(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                param.result = true
            }
        })
    }

    fun DisableSubscriptionSuggestions() {
        getDexMethod("PivotBarConstructorFingerprint") {
            findMethod {
                matcher {
                    name = "getModules"
                    declaredClass(".GenericLayoutEntry", StringMatchType.EndsWith)
                    opcodes(
                        Opcode.IGET_OBJECT,
                    )
                }
            }.single()
        }.hookMethod(object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val pageValue = param.thisObject.getObjectFieldOrNullAs<String>("page") ?: return
                if (pageValue.contains("_upsell") || pageValue.contains("promo")) {
                    param.result = Collections.EMPTY_LIST
                }
            }
        })
    }
}