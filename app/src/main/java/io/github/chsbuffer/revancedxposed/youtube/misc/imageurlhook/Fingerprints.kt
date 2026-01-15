package io.github.chsbuffer.revancedxposed.youtube.misc.imageurlhook

import io.github.chsbuffer.revancedxposed.AccessFlags
import io.github.chsbuffer.revancedxposed.findFieldDirect
import io.github.chsbuffer.revancedxposed.fingerprint
import org.luckypray.dexkit.result.FieldUsingType

internal val onFailureFingerprint = fingerprint {
    classMatcher { className(onResponseStartedFingerprint(dexkit).className) }
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters(
        "Lorg/chromium/net/UrlRequest;",
        "Lorg/chromium/net/UrlResponseInfo;",
        "Lorg/chromium/net/CronetException;"
    )
    methodMatcher { name = "onFailed" }
}

// Acts as a parent fingerprint.
internal val onResponseStartedFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Lorg/chromium/net/UrlRequest;", "Lorg/chromium/net/UrlResponseInfo;")
    strings(
        "Content-Length",
        "Content-Type",
        "identity",
        "application/x-protobuf",
    )
    methodMatcher { name = "onResponseStarted" }
    classMatcher { superClass {descriptor("Lorg/chromium/net/UrlRequest\$Callback;")} }
}

internal val onSucceededFingerprint = fingerprint {
    classMatcher { className(onResponseStartedFingerprint(dexkit).className) }
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Lorg/chromium/net/UrlRequest;", "Lorg/chromium/net/UrlResponseInfo;")
    methodMatcher { name = "onSucceeded" }
}

internal const val CRONET_URL_REQUEST_CLASS_DESCRIPTOR = "Lorg/chromium/net/impl/CronetUrlRequest;"

internal val requestFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    classMatcher { descriptor(CRONET_URL_REQUEST_CLASS_DESCRIPTOR) }
}

val urlField = findFieldDirect {
    requestFingerprint().usingFields.first {
        it.usingType == FieldUsingType.Write &&
                it.field.typeSign == "Ljava/lang/String;"
    }.field
}

internal val messageDigestImageUrlFingerprint = fingerprint {
    classMatcher { className(messageDigestImageUrlParentFingerprint(dexkit).className) }
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("Ljava/lang/String;", "L")
}

internal val messageDigestImageUrlParentFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/String;")
    parameters()
    strings("@#&=*+-_.,:!?()/~'%;\$")
}
