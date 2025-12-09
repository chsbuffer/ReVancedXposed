package io.github.chsbuffer.revancedxposed.youtube.video.playerresponse

import io.github.chsbuffer.revancedxposed.patch

val playerResponseBeforeVideoIdHooks: MutableList<(protobuf: String, videoId: String, isShortAndOpeningOrPlaying: Boolean) -> String> =
    mutableListOf()

/**
 * Hooks the video id of every video when loaded.
 * Supports all videos and functions in all situations.
 *
 * First parameter is the video id.
 * Second parameter is if the video is a Short AND it is being opened or is currently playing.
 *
 * Hook is always called off the main thread.
 *
 * This hook is called as soon as the player response is parsed,
 * and called before many other hooks are updated such as [playerTypeHookPatch].
 *
 * Note: The video id returned here may not be the current video that's being played.
 * It's common for multiple Shorts to load at once in preparation
 * for the user swiping to the next Short.
 *
 * For most use cases, you probably want to use [io.github.chsbuffer.revancedxposed.youtube.video.videoid.videoIdHooks] instead.
 *
 * Be aware, this can be called multiple times for the same video id.
 */
val playerResponseVideoIdHooks: MutableList<(videoId: String, isShortAndOpeningOrPlaying: Boolean) -> Unit> =
    mutableListOf()
val playerResponseAfterVideoIdHooks: MutableList<(protobuf: String, videoId: String, isShortAndOpeningOrPlaying: Boolean) -> String> =
    mutableListOf()

val PlayerResponseMethodHook = patch {
    val PARAMETER_VIDEO_ID = 0
    val PARAMETER_PROTO_BUFFER = 2
    var parameterIsShortAndOpeningOrPlaying = -1
    ::playerParameterBuilderFingerprint.dexMethod.apply {
        parameterIsShortAndOpeningOrPlaying =
            paramTypeNames.zip(paramTypeNames.indices)
                .indexOfFirst { (type, i) -> i >= 10 && type == "boolean" }
    }.hookMethod {
        before { param ->
            var protobuf = param.args[PARAMETER_PROTO_BUFFER] as String
            val videoId = param.args[PARAMETER_VIDEO_ID] as String
            val isShortAndOpeningOrPlaying =
                param.args[parameterIsShortAndOpeningOrPlaying] as Boolean

            playerResponseBeforeVideoIdHooks.forEach {
                protobuf = it(protobuf, videoId, isShortAndOpeningOrPlaying)
            }
            playerResponseVideoIdHooks.forEach {
                it(videoId, isShortAndOpeningOrPlaying)
            }
            playerResponseAfterVideoIdHooks.forEach {
                protobuf = it(protobuf, videoId, isShortAndOpeningOrPlaying)
            }
            param.args[PARAMETER_PROTO_BUFFER] = protobuf
        }
    }
}