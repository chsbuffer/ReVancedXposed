package io.github.chsbuffer.revancedxposed

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import app.revanced.extension.shared.Logger
import app.revanced.extension.shared.StringRef
import app.revanced.extension.shared.Utils
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.chsbuffer.revancedxposed.BuildConfig.DEBUG
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.DexKitCacheBridge
import org.luckypray.dexkit.result.ClassData
import org.luckypray.dexkit.result.FieldData
import org.luckypray.dexkit.result.MethodData
import org.luckypray.dexkit.wrap.DexClass
import org.luckypray.dexkit.wrap.DexField
import org.luckypray.dexkit.wrap.DexMethod
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method
import kotlin.reflect.KFunction0
import kotlin.reflect.KProperty0
import kotlin.system.measureTimeMillis

private typealias HookFunction = KFunction0<Unit>

interface IHook {
    val classLoader: ClassLoader
    fun Hook()

    fun DexMethod.hookMethod(callback: XC_MethodHook) {
        XposedBridge.hookMethod(toMember(), callback)
    }

    fun DexMethod.hookMethod(block: HookDsl<IHookCallback>.() -> Unit) {
        toMember().hookMethod(block)
    }

    fun DexClass.toClass() = getInstance(classLoader)
    fun DexMethod.toMethod(): Method {
        var clz = classLoader.loadClass(className)
        do {
            return XposedHelpers.findMethodExactIfExists(clz, name, *paramTypeNames.toTypedArray())
                ?: continue
        } while (clz.superclass.also { clz = it } != null)
        throw NoSuchMethodException("Method $this not found")
    }

    fun DexMethod.toConstructor(): Constructor<*> {
        var clz = classLoader.loadClass(className)
        do {
            return XposedHelpers.findConstructorExactIfExists(clz, *paramTypeNames.toTypedArray())
                ?: continue
        } while (clz.superclass.also { clz = it } != null)
        throw NoSuchMethodException("Method $this not found")
    }

    fun DexMethod.toMember(): Member {
        return when {
            isMethod -> toMethod()
            isConstructor -> toConstructor()
            else -> throw NotImplementedError()
        }
    }

    fun DexField.toField() = getFieldInstance(classLoader)
}

@Suppress("UNCHECKED_CAST")
class SharedPrefCache(app: Application) : DexKitCacheBridge.Cache {
    val pref = app.getSharedPreferences("xprevanced", MODE_PRIVATE)!!
    private val map = mutableMapOf<String, String>().apply {
        putAll(pref.all as Map<String, String>)
    }

    override fun clearAll() {
        map.clear()
    }

    override fun get(key: String, default: String?): String? = map.getOrDefault(key, default)

    override fun getAllKeys(): Collection<String> = map.keys

    override fun getList(
        key: String, default: List<String>?
    ): List<String>? = map.getOrDefault(key, null)?.split('|') ?: default

    override fun put(key: String, value: String) {
        map.put(key, value)
    }

    override fun putList(key: String, value: List<String>) {
        map.put(key, value.joinToString("|"))
    }

    override fun remove(key: String) {
        map.remove(key)
    }

    fun saveCache() {
        val edit = pref.edit()
        map.forEach { k, v ->
            edit.putString(k, v)
        }
        edit.commit()
    }
}

class DependedHookFailedException(
    subHookName: String, exception: Throwable
) : Exception("Depended hook $subHookName failed.", exception)

@SuppressLint("CommitPrefEdits")
abstract class BaseHook(private val app: Application, val lpparam: LoadPackageParam) : IHook {
    override val classLoader = lpparam.classLoader!!

    // hooks
    abstract val hooks: Array<HookFunction>
    private val appliedHooks = mutableSetOf<HookFunction>()
    private val failedHooks = mutableListOf<HookFunction>()

    // cache
    private val moduleRel = BuildConfig.COMMIT_HASH
    private var cache = SharedPrefCache(app)
    private var dexkit = run {
        System.loadLibrary("dexkit")
        DexKitCacheBridge.init(cache)
        DexKitCacheBridge.create("", lpparam.appInfo.sourceDir)
    }

    override fun Hook() {
        val t = measureTimeMillis {
            tryLoadCache()
            try {
                applyHooks()
                handleResult()
                logDebugInfo()
            } finally {
                dexkit.close()
            }
        }
        Logger.printDebug { "${lpparam.packageName} handleLoadPackage: ${t}ms" }
    }

    @Suppress("UNCHECKED_CAST")
    private fun tryLoadCache() {
        // cache by host update time + module version
        // also no cache if is DEBUG
        val packageInfo = app.packageManager.getPackageInfo(app.packageName, 0)

        val id = "${packageInfo.lastUpdateTime}-$moduleRel"
        val cachedId = cache.get("id", null)
        val isCached = cachedId.equals(id) && !DEBUG

        Logger.printInfo { "cache ID : $id" }
        Logger.printInfo { "cached ID: ${cachedId ?: ""}" }
        Logger.printInfo { "Using cached keys: $isCached" }

        if (!isCached) {
            cache.clearAll()
            cache.put("id", id)
            Utils.showToastLong("ReVanced Xposed is initializing, please wait...")
        }
    }

    private fun applyHooks() {
        hooks.forEach { hook ->
            if (appliedHooks.contains(hook)) return@forEach
            runCatching(hook).onFailure { err ->
                XposedBridge.log(err)
                failedHooks.add(hook)
            }.onSuccess {
                appliedHooks.add(hook)
            }
        }
    }

    private fun handleResult() {
        cache.saveCache()
        val success = failedHooks.isEmpty()
        if (!success) {
            XposedBridge.log("${lpparam.appInfo.packageName} version: ${getAppVersion()}")
            Utils.showToastLong("Error while apply following Hooks:\n${failedHooks.joinToString { it.name }}")
        }
    }

    private fun logDebugInfo() {
        val success = failedHooks.isEmpty()
        if (DEBUG) {
            XposedBridge.log("${lpparam.appInfo.packageName} version: ${getAppVersion()}")
            if (success) {
                Utils.showToastLong("apply hooks success")
            }
        }
    }

    private fun getAppVersion(): String {
        val packageInfo = app.packageManager.getPackageInfo(app.packageName, 0)
        val versionName = packageInfo.versionName
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION") packageInfo.versionCode
        }
        return "$versionName ($versionCode)"
    }

    fun dependsOn(vararg hooks: HookFunction) {
        hooks.forEach { hook ->
            if (appliedHooks.contains(hook)) return@forEach
            runCatching(hook).onFailure { err ->
                throw DependedHookFailedException(hook.name, err)
            }.onSuccess {
                appliedHooks.add(hook)
            }
        }
    }

    fun ExtensionResourceHook() {
        app.addModuleAssets()
        StringRef.resources = app.resources
        StringRef.packageName = BuildConfig.APPLICATION_ID
        StringRef.packageName2 = app.packageName

        app.callMethod(
            "registerActivityLifecycleCallbacks",
            object : Application.ActivityLifecycleCallbacks {
                var handleWebView: Boolean = false

                override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
                    Logger.printDebug { "onActivityCreated $activity" }
                    if (!handleWebView) {
                        WebView(activity).destroy()
                        app.addModuleAssets()
                        StringRef.resources = app.resources
                        StringRef.packageName = BuildConfig.APPLICATION_ID
                        StringRef.packageName2 = app.packageName
                    }

                    activity.addModuleAssets()
                }

                override fun onActivityDestroyed(activity: Activity) {
                    Logger.printDebug { "onActivityDestroyed $activity" }
                }

                override fun onActivityPaused(activity: Activity) {
                    Logger.printDebug { "onActivityPaused $activity" }
                }

                override fun onActivityResumed(activity: Activity) {
                    Logger.printDebug { "onActivityResumed $activity" }
                    activity.addModuleAssets()
                }

                override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
                    Logger.printDebug { "onActivitySaveInstanceState $activity" }
                }

                override fun onActivityStarted(activity: Activity) {
                    Logger.printDebug { "onActivityStarted $activity" }
                }

                override fun onActivityStopped(activity: Activity) {
                    Logger.printDebug { "onActivityStopped $activity" }
                }
            })
    }

    fun KProperty0<FindMethodFunc>.hookMethod(block: HookDsl<IHookCallback>.() -> Unit) {
        getDexMethod(this.name, this.get()).hookMethod(block)
    }

    fun KProperty0<FindMethodFunc>.hookMethod(callback: XC_MethodHook) {
        getDexMethod(this.name, this.get()).hookMethod(callback)
    }

    val KProperty0<FindMethodFunc>.member
        get() = getDexMethod(this.name, this.get()).toMember()

    val KProperty0<FindMethodFunc>.memberOrNull
        get() = runCatching { this.member }.getOrNull()

    val KProperty0<FindMethodFunc>.method
        get() = getDexMethod(this.name, this.get()).toMethod()

    val KProperty0<FindMethodFunc>.dexMethod
        get() = getDexMethod(this.name, this.get())

    val KProperty0<FindMethodListFunc>.dexMethodList
        get() = getDexMethods(this.name, this.get())

    val KProperty0<FindFieldFunc>.field
        get() = getDexField(this.name, this.get()).toField()

    val KProperty0<FindClassFunc>.clazz
        get() = getDexClass(this.name, this.get()).toClass()

    private inline fun <reified T : Any> wrapFind(
        key: String,
        crossinline funcFunc: DexKitBridge.() -> T,
        crossinline serializer: (T) -> String
    ): DexKitBridge.() -> T? {
        return {
            try {
                funcFunc().also { Logger.printInfo { "$key Matches: ${serializer(it)}" } }
            } catch (e: Exception) {
                Logger.printInfo({ "Fingerprint $key Not Found" }, e)
                null
            }
        }
    }

    private inline fun <reified T : Any> wrapFindList(
        key: String,
        crossinline funcFunc: DexKitBridge.() -> List<T>,
        crossinline serializer: (T) -> String
    ): DexKitBridge.() -> List<T> {
        return {
            try {
                funcFunc().also {
                    Logger.printInfo { "$key Matches: ${it.joinToString { serializer(it) }}" }
                }
            } catch (e: Exception) {
                Logger.printInfo({ "Fingerprint $key Not Found" }, e)
                emptyList()
            }
        }
    }

    private inline fun getDexClass(
        key: String, crossinline findFunc: DexKitBridge.() -> ClassData
    ): DexClass = dexkit.getClassDirectOrNull(key, wrapFind(key, findFunc) { it.descriptor })!!

    private inline fun getDexMethod(
        key: String, crossinline findFunc: DexKitBridge.() -> MethodData
    ): DexMethod = dexkit.getMethodDirectOrNull(key, wrapFind(key, findFunc) { it.descriptor })!!

    private inline fun getDexField(
        key: String, crossinline findFunc: DexKitBridge.() -> FieldData
    ): DexField = dexkit.getFieldDirectOrNull(key, wrapFind(key, findFunc) { it.descriptor })!!

    private inline fun getDexMethods(
        key: String, crossinline findFunc: DexKitBridge.() -> List<MethodData>
    ): List<DexMethod> = dexkit.getMethodsDirectOrEmpty(
        key, wrapFindList(key, findFunc) { it.descriptor })
}