package io.github.chsbuffer.revancedxposed.youtube.video.information

import io.github.chsbuffer.revancedxposed.AccessFlags
import io.github.chsbuffer.revancedxposed.Opcode
import io.github.chsbuffer.revancedxposed.SkipTest
import io.github.chsbuffer.revancedxposed.findClassDirect
import io.github.chsbuffer.revancedxposed.findFieldDirect
import io.github.chsbuffer.revancedxposed.findMethodDirect
import io.github.chsbuffer.revancedxposed.fingerprint
import io.github.chsbuffer.revancedxposed.youtube.shared.VideoQualityClass
import io.github.chsbuffer.revancedxposed.youtube.shared.videoQualityChangedFingerprint
import org.luckypray.dexkit.query.enums.OpCodeMatchType
import org.luckypray.dexkit.result.ClassData
import org.luckypray.dexkit.result.FieldData
import org.luckypray.dexkit.result.FieldUsingType
import org.luckypray.dexkit.result.MethodData

@get:SkipTest
val createVideoPlayerSeekbarFingerprint = fingerprint {
    returns("V")
    strings("timed_markers_width")
}

internal val OnPlaybackSpeedItemClickParentFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("L")
    parameters("L", "Ljava/lang/String;")
    methodMatcher {
        addInvoke {
            name = "getSupportFragmentManager"
        }
    }
    classMatcher { methodCount(8) }
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IF_EQZ,
        Opcode.CHECK_CAST,
    ).also { it.matchType(OpCodeMatchType.StartsWith) }
}

/**
 * Resolves using the method found in [OnPlaybackSpeedItemClickParentFingerprint].
 */
val onPlaybackSpeedItemClickFingerprint = fingerprint {
    classMatcher { className(OnPlaybackSpeedItemClickParentFingerprint(dexkit).className) }
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "L", "I", "J")
    methodMatcher {
        name = "onItemClick"
    }
}

private fun findFieldUsedByType(method: MethodData, fieldType: ClassData): FieldData {
    val fields = method.usingFields.distinct()
    fields.singleOrNull {
        it.field.typeName == fieldType.name
    }?.let { return it.field }

    val interfaceNames = fieldType.interfaces.map { it.name }.toSet()
    return fields.single {
        it.field.typeName in interfaceNames
    }.field
}

val setPlaybackSpeedMethodReference = findMethodDirect {
    onPlaybackSpeedItemClickFingerprint().invokes.findMethod { matcher { paramTypes("float") } }
        .single()
}

val setPlaybackSpeedClass = findClassDirect { setPlaybackSpeedMethodReference().declaredClass!! }

val setPlaybackSpeedClassFieldReference = findFieldDirect {
    findFieldUsedByType(
        onPlaybackSpeedItemClickFingerprint(), setPlaybackSpeedClass()
    )
}

val setPlaybackSpeedContainerClassFieldReference = findFieldDirect {
    findFieldUsedByType(
        onPlaybackSpeedItemClickFingerprint(), setPlaybackSpeedClassFieldReference().declaredClass
    )
}

val playerControllerSetTimeReferenceFingerprint = fingerprint {
    opcodes(Opcode.INVOKE_DIRECT_RANGE, Opcode.IGET_OBJECT)
    strings("Media progress reported outside media playback: ")
}

val timeMethod = findMethodDirect {
    playerControllerSetTimeReferenceFingerprint().invokes.single { it.name == "<init>" }
}

val playerInitFingerprint = fingerprint {
    accessFlags(AccessFlags.CONSTRUCTOR)
    classMatcher {
        addEqString("playVideo called on player response with no videoStreamingData.")
    }
}

/**
 * Matched using class found in [playerInitFingerprint].
 */
val seekFingerprint = fingerprint {
    classMatcher { className(playerInitFingerprint(dexkit).className) }
    strings("Attempting to seek during an ad")
}

val seekSourceType = findClassDirect {
    seekFingerprint().paramTypes[1]
}

val videoLengthFingerprint = fingerprint {
    opcodes(
        Opcode.MOVE_RESULT_WIDE,
        Opcode.CMP_LONG,
        Opcode.IF_LEZ,
        Opcode.IGET_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_WIDE,
        Opcode.GOTO,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_WIDE,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
    )
}

val videoLengthField = findFieldDirect {
    videoLengthFingerprint().usingFields.single { it.usingType == FieldUsingType.Write && it.field.typeName == "long" }.field
}

val videoLengthHolderField = findFieldDirect {
    val videoLengthField = videoLengthField()
    videoLengthFingerprint().usingFields.single { it.usingType == FieldUsingType.Read && it.field.typeName == videoLengthField.declaredClassName }.field
}

/**
 * Matches using class found in [mdxPlayerDirectorSetVideoStageFingerprint].
 */
val mdxSeekFingerprint = fingerprint {
    classMatcher { className(mdxPlayerDirectorSetVideoStageFingerprint(dexkit).className) }
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters("J", "L")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.RETURN,
    ).apply {
        // The instruction count is necessary here to avoid matching the relative version
        // of the seek method we're after, which has the same function signature as the
        // regular one, is in the same class, and even has the exact same 3 opcodes pattern.
        matchType = OpCodeMatchType.Equals
    }
}

val mdxSeekSourceType = findClassDirect {
    mdxSeekFingerprint().paramTypes[1]
}

val mdxPlayerDirectorSetVideoStageFingerprint = fingerprint {
    strings("MdxDirector setVideoStage ad should be null when videoStage is not an Ad state ")
}

/**
 * Matches using class found in [mdxPlayerDirectorSetVideoStageFingerprint].
 */
val mdxSeekRelativeFingerprint = fingerprint {
    classMatcher { className(mdxPlayerDirectorSetVideoStageFingerprint(dexkit).className) }
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    // Return type is boolean up to 19.39, and void with 19.39+.
    parameters("J", "L")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
    )
}

/**
 * Matches using class found in [playerInitFingerprint].
 */
val seekRelativeFingerprint = fingerprint {
    classMatcher { className(playerInitFingerprint(dexkit).className) }
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    // Return type is boolean up to 19.39, and void with 19.39+.
    parameters("J", "L")
    opcodes(
        Opcode.ADD_LONG_2ADDR,
        Opcode.INVOKE_VIRTUAL,
    )
}

/**
 * Resolves with the class found in [videoQualityChangedFingerprint].
 */
val playbackSpeedMenuSpeedChangedFingerprint = fingerprint {
    classMatcher { className(videoQualityChangedFingerprint(dexkit).className) }
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters("L")
    opcodes(
        Opcode.IGET,
        Opcode.INVOKE_VIRTUAL,
        Opcode.SGET_OBJECT,
        Opcode.RETURN_OBJECT,
    )
}

val playbackSpeedClassFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("L")
    parameters("L")
    opcodes(
        Opcode.RETURN_OBJECT
    )
    methodMatcher { addEqString("PLAYBACK_RATE_MENU_BOTTOM_SHEET_FRAGMENT") }
}

@get:SkipTest
private val videoQualityFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters(
        "I", // Resolution.
        "Ljava/lang/String;", // Human readable resolution: "480p", "1080p Premium", etc
        "Z", "L"
    )
//    custom { _, classDef ->
//        classDef.type == YOUTUBE_VIDEO_QUALITY_CLASS_TYPE
//    }
}

val videoQualitySetterFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("[L", "I", "Z")
    opcodes(
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IPUT_BOOLEAN,
    )
    strings("menu_item_video_quality")
}

/**
 * Matches with the class found in [videoQualitySetterFingerprint].
 */
val setVideoQualityFingerprint = fingerprint {
    returns("V")
    parameters("L")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IGET_OBJECT,
    )
    classMatcher {
        className(videoQualitySetterFingerprint(dexkit).className)
    }
}

val onItemClickListenerClassReference =
    findFieldDirect { setVideoQualityFingerprint().usingFields[0].field }
val setQualityFieldReference = findFieldDirect { setVideoQualityFingerprint().usingFields[1].field }
val setQualityMenuIndexMethod = findMethodDirect {
    setVideoQualityFingerprint().usingFields[1].field.type.findMethod {
        matcher { addParamType { descriptor = VideoQualityClass().descriptor } }
    }.single()
}