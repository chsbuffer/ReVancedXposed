package io.github.chsbuffer.revancedxposed.youtube.misc.backgroundplayback

import io.github.chsbuffer.revancedxposed.AccessFlags
import io.github.chsbuffer.revancedxposed.Opcode
import io.github.chsbuffer.revancedxposed.RequireAppVersion
import io.github.chsbuffer.revancedxposed.accessFlags
import io.github.chsbuffer.revancedxposed.findMethodDirect
import io.github.chsbuffer.revancedxposed.findMethodListDirect
import io.github.chsbuffer.revancedxposed.fingerprint
import io.github.chsbuffer.revancedxposed.literal
import io.github.chsbuffer.revancedxposed.parameters
import io.github.chsbuffer.revancedxposed.resourceMappings
import io.github.chsbuffer.revancedxposed.returns

val prefBackgroundAndOfflineCategoryId get() = resourceMappings["string", "pref_background_and_offline_category"]

val backgroundPlaybackManagerFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Z")
    parameters("L")
    opcodes(
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.IGET,
        Opcode.AND_INT_LIT16,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.IGET,
        Opcode.CONST,
        Opcode.IF_NE,
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.IGET,
        Opcode.IF_NE,
        Opcode.IGET_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.GOTO,
        Opcode.SGET_OBJECT,
        Opcode.GOTO,
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQZ,
    )
}

val backgroundPlaybackSettingsFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/String;")
    parameters()
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.IF_NEZ,
        Opcode.GOTO,
    )
    literal { prefBackgroundAndOfflineCategoryId }
}

val backgroundPlaybackSettingsSubFingerprint = findMethodDirect {
    backgroundPlaybackSettingsFingerprint().invokes.filter { it.returnTypeName == "boolean" }[1]
}

val kidsBackgroundPlaybackPolicyControllerFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("I", "L", "L")
    opcodes(
        Opcode.CONST_4,
        Opcode.IF_NE,
        Opcode.SGET_OBJECT,
        Opcode.IF_NE,
        Opcode.IGET,
        Opcode.CONST_4,
        Opcode.IF_NE,
        Opcode.IGET_OBJECT,
    )
    literal { 5 }
}

val backgroundPlaybackManagerShortsFingerprint = findMethodListDirect {
    /*
    * two matches in versions 21.02.32
    * It doesn't seem to be an A/B test;
    * it seems to be a different method that checks an additional property to determine the result.
    * */
    findMethod {
        matcher {
            accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
            returns("Z")
            parameters("L")
            literal { 151635310 }
        }
    }
}

val shortsBackgroundPlaybackFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    literal { 45415425 }
}

internal const val PIP_INPUT_CONSUMER_FEATURE_FLAG = 45638483L

// Fix 'E/InputDispatcher: Window handle pip_input_consumer has no registered input channel'
@get:RequireAppVersion("19.34.00")
val pipInputConsumerFeatureFlagFingerprint = fingerprint {
    literal { PIP_INPUT_CONSUMER_FEATURE_FLAG }
}