package io.github.chsbuffer.revancedxposed.youtube.layout.startupshortsreset

import io.github.chsbuffer.revancedxposed.AccessFlags
import io.github.chsbuffer.revancedxposed.Opcode
import io.github.chsbuffer.revancedxposed.accessFlags
import io.github.chsbuffer.revancedxposed.findMethodDirect
import io.github.chsbuffer.revancedxposed.fingerprint
import io.github.chsbuffer.revancedxposed.opcodes
import io.github.chsbuffer.revancedxposed.parameters
import io.github.chsbuffer.revancedxposed.returns

val userWasInShortsFingerprint = findMethodDirect {
    runCatching {
        fingerprint {
            returns("V")
            accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
            parameters("Ljava/lang/Object;")
            strings("userIsInShorts: ")
        }
    }.getOrElse {
        findMethod {
            matcher {
                returns("V")
                accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
                parameters("Ljava/lang/Object;")
                opcodes(
                    Opcode.INVOKE_INTERFACE, // userWasInShortsProtoStoreProvider
                    Opcode.MOVE_RESULT_OBJECT,
                    Opcode.CHECK_CAST,
                    Opcode.NEW_INSTANCE,
                    Opcode.INVOKE_DIRECT, // userWasInShortsBuilder
                    Opcode.INVOKE_INTERFACE,
                    Opcode.RETURN_VOID,
                )
            }
        }.findMethod {
            matcher {
                opcodes(
                    Opcode.CHECK_CAST, // p1, Ljava/lang/Boolean; // userIsInShorts
                    Opcode.INVOKE_VIRTUAL, // Ljava/lang/Boolean;->booleanValue()Z
                    Opcode.MOVE_RESULT,
                    Opcode.IGET_OBJECT,
                    Opcode.MOVE_OBJECT,
                    Opcode.CHECK_CAST,
                    Opcode.IGET_OBJECT,
                    Opcode.INVOKE_INTERFACE, // userWasInShortsProtoStoreProvider
                )
            }
        }.single()
    }
}

val userWasInShortsBuilderFingerprint = findMethodDirect {
    val paramTypes = listOf("boolean", "int")
    userWasInShortsFingerprint().invokes.single {
        it.paramTypeNames.takeLast(2) == paramTypes
    }
}

/**
 * 18.15.40+
 */
val userWasInShortsConfigFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    literal {
        45358360L
    }
}
