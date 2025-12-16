package io.github.chsbuffer.revancedxposed.music.layout.upgradebutton

import io.github.chsbuffer.revancedxposed.AccessFlags
import io.github.chsbuffer.revancedxposed.Opcode
import io.github.chsbuffer.revancedxposed.findFieldDirect
import io.github.chsbuffer.revancedxposed.fingerprint

internal val pivotBarConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    parameters("L", "Z")
    opcodes(
        Opcode.INVOKE_INTERFACE,
        Opcode.GOTO,
        Opcode.IPUT_OBJECT,
        Opcode.RETURN_VOID
    )
}

val pivotBarElementField = findFieldDirect {
    pivotBarConstructorFingerprint().declaredClass!!.fields.single { f -> f.typeName == "java.util.List" }
}