package io.github.chsbuffer.revancedxposed.youtube.layout.autocaptions

import io.github.chsbuffer.revancedxposed.AccessFlags
import io.github.chsbuffer.revancedxposed.Opcode
import io.github.chsbuffer.revancedxposed.fingerprint

internal val startVideoInformerFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    opcodes(
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID,
    )
    methodMatcher {
        addEqString("pc")
    }
}

internal val storyboardRendererDecoderRecommendedLevelFingerprint = fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("L")
    strings("#-1#")
}

internal val subtitleTrackFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    strings("DISABLE_CAPTIONS_OPTION")
}
