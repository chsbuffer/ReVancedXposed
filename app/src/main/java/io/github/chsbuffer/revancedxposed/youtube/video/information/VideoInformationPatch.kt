package io.github.chsbuffer.revancedxposed.youtube.video.information

import app.revanced.extension.shared.Logger
import app.revanced.extension.youtube.patches.VideoInformation
import com.google.android.libraries.youtube.innertube.model.media.VideoQuality
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import io.github.chsbuffer.revancedxposed.findFirstFieldByExactType
import io.github.chsbuffer.revancedxposed.getStaticObjectField
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.scopedHook
import io.github.chsbuffer.revancedxposed.youtube.video.playerresponse.PlayerResponseMethodHook
import io.github.chsbuffer.revancedxposed.youtube.video.playerresponse.playerResponseBeforeVideoIdHooks
import io.github.chsbuffer.revancedxposed.youtube.video.playerresponse.playerResponseVideoIdHooks
import io.github.chsbuffer.revancedxposed.youtube.video.videoid.VideoId
import io.github.chsbuffer.revancedxposed.youtube.video.videoid.videoIdHooks
import org.luckypray.dexkit.wrap.DexClass
import java.lang.ref.WeakReference
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Hook the player controller.  Called when a video is opened or the current video is changed.
 *
 * Note: This hook is called very early and is called before the video id, video time, video length,
 * and many other data fields are set.
 *
 * @param targetMethodClass The descriptor for the class to invoke when the player controller is created.
 * @param targetMethodName The name of the static method to invoke when the player controller is created.
 */
val onCreateHook = mutableListOf<(VideoInformation.PlaybackController) -> Unit>()
val videoTimeHooks = mutableListOf<(Long) -> Unit>()

/*
 * Hook when the video speed is changed for any reason _except when the user manually selects a new speed_.
 * */
val videoSpeedChangedHook = mutableListOf<(Float) -> Unit>()

/**
 * Hook the video speed selected by the user.
 */
val userSelectedPlaybackSpeedHook = mutableListOf<(Float) -> Unit>()

lateinit var setPlaybackSpeedMethod: Method
lateinit var setPlaybackSpeedClassField: Field
lateinit var setPlaybackSpeedContainerClassField: Field

private var playbackSpeedClass: Any? = null

fun doOverridePlaybackSpeed(speedOverride: Float) {
    val setPlaybackSpeedObj = playbackSpeedClass.let { setPlaybackSpeedContainerClassField.get(it) }
    if (speedOverride <= 0.0f || setPlaybackSpeedObj == null)
        return

    setPlaybackSpeedObj
        .let { setPlaybackSpeedClassField.get(it) }
        .let { setPlaybackSpeedMethod(it, speedOverride) }
}

class PlaybackController(
    obj: Any,
    private val seekTo: Method,
    private val seekToRelative: Method,
    val seekSourceNone: Any
) : VideoInformation.PlaybackController {
    val obj = WeakReference(obj)

    init {
        XposedHelpers.setAdditionalInstanceField(obj, "patch_controller", this)
    }

    override fun patch_seekTo(videoTime: Long): Boolean {
        return seekTo(obj.get(), videoTime, seekSourceNone) as Boolean
    }

    override fun patch_seekToRelative(videoTimeOffset: Long) {
        seekToRelative(obj.get(), videoTimeOffset, seekSourceNone)
    }
}

private lateinit var getQualityName: (VideoQuality) -> String
private lateinit var getResolution: (VideoQuality) -> Int

fun VideoQuality.getResolution() = getResolution(this)
fun VideoQuality.getQualityName() = getQualityName(this)

val VideoInformationPatch = patch(
    description = "Hooks YouTube to get information about the current playing video.",
) {
    dependsOn(
        VideoId,
        PlayerResponseMethodHook,
    )

    //region playerController
    ::playerInitFingerprint.apply {
        val seekSourceType = ::seekSourceType.clazz
        val seekSourceNone = seekSourceType.getStaticObjectField("a")!!
        hookMethod {
            val seekFingerprint = ::seekFingerprint.method
            val seekRelativeFingerprint = ::seekRelativeFingerprint.method

            after { param ->
                val playerController = PlaybackController(
                    param.thisObject, seekFingerprint, seekRelativeFingerprint, seekSourceNone
                )
                onCreateHook.forEach { it(playerController) }
            }
        }
    }

    //endregion

    //region mdxPlayerDirector
    ::mdxPlayerDirectorSetVideoStageFingerprint.apply {
        val seekSourceType = ::mdxSeekSourceType.clazz
        val seekSourceNone = seekSourceType.getStaticObjectField("a")!!
        hookMethod {
            val mdxSeekFingerprint = ::mdxSeekFingerprint.method
            val mdxSeekRelativeFingerprint = ::mdxSeekRelativeFingerprint.method

            after { param ->
                val playerController = PlaybackController(
                    param.thisObject, mdxSeekFingerprint, mdxSeekRelativeFingerprint, seekSourceNone
                )
                VideoInformation.initializeMdx(playerController)
            }
        }
    }
    //endregion

    ::videoLengthFingerprint.hookMethod {
        val videoLengthField = ::videoLengthField.field
        val videoLengthHolderField = ::videoLengthHolderField.field

        after { param ->
            val videoLength = param.thisObject
                .let { videoLengthHolderField.get(it) }
                .let { videoLengthField.getLong(it) }
            VideoInformation.setVideoLength(videoLength)
        }
    }

    /*
     * Inject call for video ids
     */
    videoIdHooks.add { VideoInformation.setVideoId(it) }
    playerResponseVideoIdHooks.add { id, z -> VideoInformation.setPlayerResponseVideoId(id, z) }

    // Call before any other video id hooks,
    // so they can use VideoInformation and check if the video id is for a Short.
    playerResponseBeforeVideoIdHooks.add { protobuf, videoId, isShortAndOpeningOrPlaying ->
        VideoInformation.newPlayerResponseSignature(
            protobuf, videoId, isShortAndOpeningOrPlaying
        )
    }

    /*
     * Set the video time method
     */
    ::timeMethod.hookMethod {
        before { param ->
            val videoTime = param.args[0] as Long
            videoTimeHooks.forEach { it(videoTime) }
        }
    }

    /*
     * Hook the methods which set the time
     */
    videoTimeHooks.add { videoTime ->
        VideoInformation.setVideoTime(videoTime)
    }

    /*
     * Hook the user playback speed selection.
     */
    setPlaybackSpeedMethod = ::setPlaybackSpeedMethodReference.method
    setPlaybackSpeedClassField = ::setPlaybackSpeedClassFieldReference.field
    setPlaybackSpeedContainerClassField = ::setPlaybackSpeedContainerClassFieldReference.field

    ::setPlaybackSpeedMethodReference.hookMethod {
        before { param ->
            // Hook when the video speed is changed for any reason _except when the user manually selects a new speed_.
            videoSpeedChangedHook.forEach { it(param.args[0] as Float) }
        }
    }

    ::onPlaybackSpeedItemClickFingerprint.hookMethod(scopedHook(::setPlaybackSpeedMethodReference.member) {
        before { param ->
            // Hook the video speed selected by the user.
            Logger.printDebug { "onPlaybackSpeedItemClickFingerprint: ${param.args[0]}" }
            userSelectedPlaybackSpeedHook.forEach { it.invoke(param.args[0] as Float) }
            videoSpeedChangedHook.forEach { it.invoke(param.args[0] as Float) }
        }
    })

    ::playbackSpeedClassFingerprint.hookMethod {
        // Set playback speed class.
        after { playbackSpeedClass = it.result }
    }

    // Handle new playback speed menu.
    ::playbackSpeedMenuSpeedChangedFingerprint.hookMethod(scopedHook(::setPlaybackSpeedMethodReference.member) {
        before { param ->
            Logger.printDebug { "Playback speed menu speed changed: ${param.args[0]}" }
            userSelectedPlaybackSpeedHook.forEach { it.invoke(param.args[0] as Float) }
            videoSpeedChangedHook.forEach { it.invoke(param.args[0] as Float) }
        }
    })

    // videoQuality
    val YOUTUBE_VIDEO_QUALITY_CLASS_TYPE =
        "Lcom/google/android/libraries/youtube/innertube/model/media/VideoQuality;"

    val videoQualityClass = DexClass(YOUTUBE_VIDEO_QUALITY_CLASS_TYPE).toClass()
    val qualityNameField = videoQualityClass.findFirstFieldByExactType(String::class.java)
    val resolutionField = videoQualityClass.findFirstFieldByExactType(Int::class.java)

    getQualityName = { quality -> qualityNameField.get(quality) as String }
    getResolution = { quality -> resolutionField.get(quality) as Int }

    // Fix bad data used by YouTube.
    XposedBridge.hookAllConstructors(
        videoQualityClass, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val quality = param.thisObject as VideoQuality
                val newResolution = VideoInformation.fixVideoQualityResolution(
                    quality.getQualityName(), quality.getResolution()
                )
                resolutionField.set(quality, newResolution)
            }
        })

    // Detect video quality changes and override the current quality.
    ::videoQualitySetterFingerprint.hookMethod {
        val onItemClickListenerClass = ::onItemClickListenerClassReference.field
        val setQualityField = ::setQualityFieldReference.field
        val setQualityMenuIndexMethod = ::setQualityMenuIndexMethod.method

        @Suppress("UNCHECKED_CAST") before { param ->
            val qualities = param.args[0] as Array<out VideoQuality>
            val originalQualityIndex = param.args[1] as Int
            val menu = param.thisObject.let { onItemClickListenerClass.get(it) }
                .let { setQualityField.get(it) }

            param.args[1] = VideoInformation.setVideoQuality(
                qualities,
                { quality -> setQualityMenuIndexMethod(menu, quality) },
                originalQualityIndex
            )
        }
    }

    onCreateHook.add { VideoInformation.initialize(it) }
    videoSpeedChangedHook.add { VideoInformation.videoSpeedChanged(it) }
    userSelectedPlaybackSpeedHook.add { VideoInformation.userSelectedPlaybackSpeed(it) }
}