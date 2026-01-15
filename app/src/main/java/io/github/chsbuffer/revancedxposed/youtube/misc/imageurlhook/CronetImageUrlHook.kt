package io.github.chsbuffer.revancedxposed.youtube.misc.imageurlhook

import io.github.chsbuffer.revancedxposed.patch
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import org.chromium.net.impl.CronetUrlRequest
import java.io.IOException
import java.lang.reflect.Field

val cronetImageUrlHookPatch = patch(
    description = "Hooks Cronet image urls.",
) {
    // loadImageUrlMethod
    ::messageDigestImageUrlFingerprint.hookMethod {
        before { param ->
            var url = param.args[0] as String? ?: return@before
            imageUrlHooks.forEach {
                url = it(url)
            }
            param.args[0] = url
        }
    }
    // loadImageSuccessCallbackMethod
    ::onSucceededFingerprint.hookMethod {
        before { param ->
            imageUrlSuccessCallbackHook.forEach {
                it(param.args[0] as UrlRequest, param.args[1] as UrlResponseInfo)
            }
        }
    }

    // loadImageErrorCallbackMethod
    ::onFailureFingerprint.hookMethod {
        before { param ->
            imageUrlErrorCallbackHook.forEach {
                it(
                    param.args[0] as UrlRequest,
                    param.args[1] as UrlResponseInfo,
                    param.args[2] as IOException
                )
            }
        }
    }

    // The URL is required for the failure callback hook, but the URL field is obfuscated.
    // Add a helper get method that returns the URL field.
    urlJField = ::urlField.field
}

private lateinit var urlJField: Field

fun getHookedUrl(o: CronetUrlRequest) = urlJField.get(o) as String

private var imageUrlHooks = listOf<(String) -> String>()
private var imageUrlSuccessCallbackHook = listOf<(UrlRequest, UrlResponseInfo) -> Unit>()
private var imageUrlErrorCallbackHook = listOf<(UrlRequest, UrlResponseInfo, IOException) -> Unit>()

/**
 * @param highPriority If the hook should be called before all other hooks.
 */
fun addImageUrlHook(f: (String) -> String, highPriority: Boolean = false) {
    imageUrlHooks = if (highPriority) listOf(f) + imageUrlHooks else imageUrlHooks + listOf(f)
}

/**
 * If a connection completed, which includes normal 200 responses but also includes
 * status 404 and other error like http responses.
 */
fun addImageUrlSuccessCallbackHook(f: (UrlRequest, UrlResponseInfo) -> Unit) {
    imageUrlSuccessCallbackHook += listOf(f)
}

/**
 * If a connection outright failed to complete any connection.
 */
fun addImageUrlErrorCallbackHook(f: (UrlRequest, UrlResponseInfo, IOException) -> Unit) {
    imageUrlErrorCallbackHook += listOf(f)
}
