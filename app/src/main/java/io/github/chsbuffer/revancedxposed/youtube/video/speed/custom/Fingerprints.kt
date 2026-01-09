package io.github.chsbuffer.revancedxposed.youtube.video.speed.custom

import io.github.chsbuffer.revancedxposed.AccessFlags
import io.github.chsbuffer.revancedxposed.Opcode
import io.github.chsbuffer.revancedxposed.RequireAppVersion
import io.github.chsbuffer.revancedxposed.findFieldDirect
import io.github.chsbuffer.revancedxposed.findMethodDirect
import io.github.chsbuffer.revancedxposed.fingerprint
import io.github.chsbuffer.revancedxposed.parameters
import io.github.chsbuffer.revancedxposed.resourceMappings
import io.github.chsbuffer.revancedxposed.returns
import io.github.chsbuffer.revancedxposed.youtube.video.information.setPlaybackSpeedClass
import io.github.chsbuffer.revancedxposed.youtube.video.information.setPlaybackSpeedMethodReference

internal val getOldPlaybackSpeedsFingerprint = fingerprint {
    parameters("[L", "I")
    strings("menu_item_playback_speed")
}

val speedUnavailableId get() = resourceMappings["string", "varispeed_unavailable_message"]

internal val showOldPlaybackSpeedMenuFingerprint = fingerprint {
    literal { speedUnavailableId }
}

internal val speedArrayGeneratorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("[L")
    parameters("L")
    strings("0.0#")
}

// found in com.google.android.libraries.youtube.innertube.model.media.PlayerConfigModel
val speedsFloatArrayField = findFieldDirect {
    speedArrayGeneratorFingerprint().usingFields.single {
        it.field.typeSign == "[F"
    }.field
}

internal val speedLimiterFingerprint = findMethodDirect {
    runCatching {
        fingerprint {
            accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
            returns("V")
            parameters("F")
            opcodes(
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT,
                Opcode.IF_EQZ,
                Opcode.CONST_HIGH16,
                Opcode.GOTO,
                Opcode.CONST_HIGH16,
                Opcode.CONST_HIGH16,
                Opcode.INVOKE_STATIC,
            )
        }
    }.getOrElse {
        fingerprint {
            strings("setPlaybackRate")
            methodMatcher {
                addInvoke {
                    parameters("F", "F", "F")
                    returns("F")
                }
            }
        }
    }
}

val clampFloatFingerprint = findMethodDirect {
    speedLimiterFingerprint().invokes.findMethod {
        matcher {
            parameters("F", "F", "F")
            returns("F")
        }
    }.single()
}

val getPlaybackSpeedMethodReference = findMethodDirect {
    setPlaybackSpeedClass().findMethod {
        matcher {
            returns("F")
            addUsingNumber(1.0f)
        }
    }.single()
}

@get:RequireAppVersion("19.25.00")
val onSpeedTapAndHoldFingerprint = findMethodDirect {
    findMethod {
        matcher {
            addInvoke { descriptor = getPlaybackSpeedMethodReference().descriptor }
            addInvoke { descriptor = setPlaybackSpeedMethodReference().descriptor }
            addInvoke { name = "removeCallbacks" }
            addUsingNumber(2.0f)
        }
    }.single()
}
