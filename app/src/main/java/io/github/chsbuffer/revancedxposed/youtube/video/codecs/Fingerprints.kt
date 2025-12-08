package io.github.chsbuffer.revancedxposed.youtube.video.codecs

import io.github.chsbuffer.revancedxposed.AccessFlags
import io.github.chsbuffer.revancedxposed.fingerprint

internal val vp9CapabilityFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    strings(
        "vp9_supported",
        "video/x-vnd.on2.vp9"
    )
}
