package io.github.chsbuffer.revancedxposed.youtube.layout.sponsorblock

import io.github.chsbuffer.revancedxposed.AccessFlags
import io.github.chsbuffer.revancedxposed.Opcode
import io.github.chsbuffer.revancedxposed.accessFlags
import io.github.chsbuffer.revancedxposed.findFieldDirect
import io.github.chsbuffer.revancedxposed.findMethodDirect
import io.github.chsbuffer.revancedxposed.fingerprint
import io.github.chsbuffer.revancedxposed.parameters
import io.github.chsbuffer.revancedxposed.resourceMappings
import io.github.chsbuffer.revancedxposed.returns
import io.github.chsbuffer.revancedxposed.youtube.shared.seekbarFingerprint

internal val appendTimeFingerprint = fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Ljava/lang/CharSequence;", "Ljava/lang/CharSequence;", "Ljava/lang/CharSequence;")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
    )
}

val SponsorBarRect = findFieldDirect {
    val clazz = seekbarFingerprint().declaredClass!!
    clazz.findMethod {
        matcher {
            addInvoke {
                name = "invalidate"
                paramTypes("android.graphics.Rect")
            }
        }
    }.single().usingFields.last { it.field.typeName == "android.graphics.Rect" }.field
}

val seekbarOnDrawFingerprint = findMethodDirect {
    seekbarFingerprint().declaredClass!!.findMethod {
        matcher {
            name = "onDraw"
        }
    }.single()
}

val inset_overlay_view_layout get() = resourceMappings["id", "inset_overlay_view_layout"]

val controlsOverlayFingerprint = findMethodDirect {
    findMethod {
        matcher {
            addUsingNumber(inset_overlay_view_layout)
            paramCount = 0
            returnType = "void"
        }
    }.single()
}

val AdProgressTextVisibility = fingerprint {
    definingClass("Lcom/google/android/libraries/youtube/ads/player/ui/AdProgressTextView;")
    name("setVisibility")
}

internal val adProgressTextViewVisibilityFingerprint = findMethodDirect {
    AdProgressTextVisibility().callers.findMethod {
        matcher {
            accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
            returns("V")
            parameters("Z")
        }
    }.single()
}