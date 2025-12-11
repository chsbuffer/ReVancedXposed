package io.github.chsbuffer.revancedxposed

import android.app.Application
import app.revanced.extension.shared.Utils
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.chsbuffer.revancedxposed.common.UpdateChecker
import io.github.chsbuffer.revancedxposed.googlephotos.GooglePhotosHook
import io.github.chsbuffer.revancedxposed.meta.MetaHook
import io.github.chsbuffer.revancedxposed.music.MusicHook
import io.github.chsbuffer.revancedxposed.photomath.PhotomathHook
import io.github.chsbuffer.revancedxposed.reddit.RedditHook
import io.github.chsbuffer.revancedxposed.strava.StravaHook
import io.github.chsbuffer.revancedxposed.youtube.YoutubeHook

class MainHook : IXposedHookLoadPackage, IXposedHookZygoteInit {
    lateinit var startupParam: StartupParam
    lateinit var lpparam: LoadPackageParam
    lateinit var app: Application
    var targetPackageName: String? = null
    val hooksByPackage = mapOf(
        "com.google.android.apps.youtube.music" to { MusicHook(app, lpparam) },
        "com.google.android.youtube" to { YoutubeHook(app, lpparam) },
        "com.reddit.frontpage" to { RedditHook(app, lpparam) },
        "com.google.android.apps.photos" to { GooglePhotosHook(lpparam) },
        "com.instagram.android" to { MetaHook(app, lpparam) },
        "com.instagram.barcelona" to { MetaHook(app, lpparam) },
        "com.strava" to { StravaHook(app, lpparam) },
        "com.microblink.photomath" to { PhotomathHook(app, lpparam) },
    )

    fun shouldHook(packageName: String): Boolean {
        if (!hooksByPackage.containsKey(packageName)) return false
        if (targetPackageName == null) targetPackageName = packageName
        return targetPackageName == packageName
    }

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (!lpparam.isFirstApplication) return
        if (!shouldHook(lpparam.packageName)) return
        this.lpparam = lpparam

        inContext(lpparam) { app ->
            this.app = app
            if (isReVancedPatched(lpparam)) {
                Utils.showToastLong("ReVanced Xposed module does not work with patched app")
                return@inContext
            }

            resourceMappings = object : ResourceFinder {
                override operator fun get(type: String, name: String): Int {
                    val id = Utils.getResourceIdentifier(name, type)
                    if (id == 0) throw Exception("Could not find resource type: $type name: $name")
                    return id
                }
            }

            DebugHook(lpparam.classLoader).Hook()
            hooksByPackage[lpparam.packageName]?.invoke()?.Hook()
        }
    }

    private fun isReVancedPatched(lpparam: LoadPackageParam): Boolean {
        return runCatching {
            lpparam.classLoader.loadClass("app.revanced.extension.shared.Utils")
        }.isSuccess || runCatching {
            lpparam.classLoader.loadClass("app.revanced.extension.shared.utils.Utils")
        }.isSuccess || runCatching {
            lpparam.classLoader.loadClass("app.revanced.integrations.shared.Utils")
        }.isSuccess || runCatching {
            lpparam.classLoader.loadClass("app.revanced.integrations.shared.utils.Utils")
        }.isSuccess
    }

    override fun initZygote(startupParam: StartupParam) {
        this.startupParam = startupParam
        XposedInit = startupParam
    }
}

fun inContext(lpparam: LoadPackageParam, f: (Application) -> Unit) {
    val appClazz = XposedHelpers.findClass(lpparam.appInfo.className, lpparam.classLoader)
    XposedBridge.hookMethod(appClazz.getMethod("onCreate"), object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            val app = param.thisObject as Application
            Utils.setContext(app)
            f(app)
            if (XposedInit.modulePath.startsWith("/data/app/"))
                UpdateChecker().hookNewActivity()
        }
    })
}
