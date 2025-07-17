package io.github.chsbuffer.revancedxposed

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import app.revanced.extension.shared.Logger
import app.revanced.extension.shared.Utils
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.chsbuffer.revancedxposed.BuildConfig.DEBUG
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.result.ClassData
import org.luckypray.dexkit.result.FieldData
import org.luckypray.dexkit.result.MethodData
import org.luckypray.dexkit.wrap.DexClass
import org.luckypray.dexkit.wrap.DexField
import org.luckypray.dexkit.wrap.DexMethod
import java.lang.reflect.Member
import kotlin.reflect.KFunction0
import kotlin.system.measureTimeMillis

private typealias HookFunction = KFunction0<Unit>

interface IHook {
    val classLoader: ClassLoader
    fun Hook()

    fun DexMethod.hookMethod(callback: XC_MethodHook) {
        XposedBridge.hookMethod(toMember(), callback)    }

    fun Member.hookMethod(callback: XC_MethodHook) {
        XposedBridge.hookMethod(this, callback)    }

    fun DexClass.toClass() = getInstance(classLoader)
    fun DexMethod.toMethod() = getMethodInstance(classLoader)
    fun DexMethod.toMember() = when {
        isMethod -> getMethodInstance(classLoader)
        isConstructor -> getConstructorInstance(classLoader)
        else -> throw NotImplementedError()
    }
    fun DexField.toField() = getFieldInstance(classLoader)
}

class DependedHookFailedException(
    subHookName: String, exception: Throwable
) : Exception("Depended hook $subHookName failed.", exception)

@SuppressLint("CommitPrefEdits")
abstract class BaseHook(val app: Application, val lpparam: LoadPackageParam) : IHook {
    override val classLoader = lpparam.classLoader!!

    // hooks
    abstract val hooks: Array<HookFunction>
    private val appliedHooks = mutableSetOf<HookFunction>()
    private val failedHooks = mutableListOf<HookFunction>()

    // cache
    private val moduleRel = BuildConfig.VERSION_CODE
    private var pref = app.getSharedPreferences("xprevanced", Context.MODE_PRIVATE)!!
    private val map = mutableMapOf<String, String>()
    private lateinit var dexkit: DexKitBridge
    private var isCached: Boolean = false

    override fun Hook() {
        val t = measureTimeMillis {
            tryLoadCache()
            try {
                applyHooks()
                handleResult()
                logDebugInfo()
            } finally {
                closeDexKit()
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
        val cachedId = pref.getString("id", null)
        isCached = cachedId.equals(id) && !DEBUG

        Logger.printInfo { "cache ID : $id" }
        Logger.printInfo { "cached ID: ${cachedId ?: ""}" }
        Logger.printInfo { "Using cached keys: $isCached" }

        if (isCached) {
            map.putAll(pref.all as Map<String, String>)
        } else {
            map["id"] = id
            createDexKit()
        }
    }

    private fun createDexKit() {
        System.loadLibrary("dexkit")
        dexkit = DexKitBridge.create(lpparam.appInfo.sourceDir)
    }

    private fun closeDexKit() {
        if (::dexkit.isInitialized) {
            dexkit.close()
        }
    }

    private fun applyHooks() {
        hooks.forEach { hook ->
            runCatching(hook).onFailure { err ->
                XposedBridge.log(err)
                failedHooks.add(hook)
            }.onSuccess {
                appliedHooks.add(hook)
            }
        }
    }

    private fun handleResult() {
        // save cache if no failure
        val success = failedHooks.isEmpty()
        if (!success) {
            XposedBridge.log("${lpparam.appInfo.packageName} version: ${getAppVersion()}")
            Utils.showToastLong("Error while apply following Hooks:\n${failedHooks.joinToString { it.name }}")

            clearCache()
        } else {
            saveCache()
        }
    }

    private fun logDebugInfo() {
        val success = failedHooks.isEmpty()
        if (DEBUG) {
            XposedBridge.log("${lpparam.appInfo.packageName} version: ${getAppVersion()}")
            if (success) {
                Utils.showToastLong("apply hooks success")
            }
            if (success && isCached) {
                map.forEach { key, value -> Logger.printDebug { "$key Matches: $value" } }
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

    private fun saveCache() {
        if (isCached) return

        val edit = pref.edit()
        map.forEach { k, v ->
            edit.putString(k, v)
        }
        edit.commit()
    }

    private fun clearCache() {
        Logger.printInfo { "clear cache" }
        pref.edit().clear().commit()
        isCached = false
        map.clear()
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

    private fun <T, R> getFromCacheOrFind(
        key: String,
        findFunc: (DexKitBridge.() -> T)?,
        serialize: (T) -> String,
        deserialize: (String) -> R
    ): R {
        return map[key]?.let { deserialize(it) } ?: findFunc!!(dexkit).let { result ->
            val serializedValue = serialize(result)
            map[key] = serializedValue
            Logger.printInfo { "$key Matches: $serializedValue" }
            deserialize(serializedValue)
        }
    }

    fun getDexClass(key: String, findFunc: (DexKitBridge.() -> ClassData)? = null): DexClass =
        getFromCacheOrFind(key, findFunc, { it.descriptor }, { DexClass(it) })

    fun getDexMethod(key: String, findFunc: (DexKitBridge.() -> MethodData)? = null): DexMethod =
        getFromCacheOrFind(key, findFunc, { it.descriptor }, { DexMethod(it) })

    fun getDexMethods(
        key: String,
        findFunc: (DexKitBridge.() -> Iterable<MethodData>)? = null
    ): Iterable<DexMethod> =
        getFromCacheOrFind(
            key, findFunc,
            { it.joinToString("|") { it.descriptor } },
            { it.split("|").map { DexMethod(it) } })

    fun getDexField(key: String, findFunc: (DexKitBridge.() -> FieldData)? = null): DexField =
        getFromCacheOrFind(key, findFunc, { it.descriptor }, { DexField(it) })

    fun getString(key: String, findFunc: (DexKitBridge.() -> String)? = null): String =
        getFromCacheOrFind(key, findFunc, { it }, { it })

    fun getNumber(key: String, findFunc: (DexKitBridge.() -> Int)? = null): Int =
        getFromCacheOrFind(key, findFunc, { it.toString() }, { Integer.parseInt(it) })

}