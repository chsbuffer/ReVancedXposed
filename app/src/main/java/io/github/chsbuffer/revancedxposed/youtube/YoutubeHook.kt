package io.github.chsbuffer.revancedxposed.youtube

import android.app.Activity
import android.app.Application
import app.revanced.extension.shared.Utils
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.chsbuffer.revancedxposed.BaseHook
import io.github.chsbuffer.revancedxposed.addModuleAssets
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.injectHostClassLoaderToSelf
import io.github.chsbuffer.revancedxposed.shared.misc.CheckRecycleBitmapMediaSession
import io.github.chsbuffer.revancedxposed.youtube.ad.general.HideAds
import io.github.chsbuffer.revancedxposed.youtube.ad.video.VideoAds
import io.github.chsbuffer.revancedxposed.youtube.interaction.copyvideourl.CopyVideoUrl
import io.github.chsbuffer.revancedxposed.youtube.interaction.downloads.Downloads
import io.github.chsbuffer.revancedxposed.youtube.interaction.swipecontrols.SwipeControls
import io.github.chsbuffer.revancedxposed.youtube.layout.buttons.action.HideButtons
import io.github.chsbuffer.revancedxposed.youtube.layout.buttons.navigation.NavigationButtons
import io.github.chsbuffer.revancedxposed.youtube.layout.hide.general.HideLayoutComponents
import io.github.chsbuffer.revancedxposed.youtube.layout.hide.shorts.HideShortsComponents
import io.github.chsbuffer.revancedxposed.youtube.layout.sponsorblock.SponsorBlock
import io.github.chsbuffer.revancedxposed.youtube.layout.startupshortsreset.DisableResumingShortsOnStartup
import io.github.chsbuffer.revancedxposed.youtube.misc.backgroundplayback.BackgroundPlayback
import io.github.chsbuffer.revancedxposed.youtube.misc.debugging.EnableDebugging
import io.github.chsbuffer.revancedxposed.youtube.misc.privacy.SanitizeSharingLinks
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.SettingsHook
import io.github.chsbuffer.revancedxposed.youtube.shared.YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE
import io.github.chsbuffer.revancedxposed.youtube.video.audio.ForceOriginalAudio
import io.github.chsbuffer.revancedxposed.youtube.video.codecs.DisableVideoCodecs
import io.github.chsbuffer.revancedxposed.youtube.video.quality.VideoQuality
import io.github.chsbuffer.revancedxposed.youtube.video.speed.PlaybackSpeed
import org.luckypray.dexkit.wrap.DexMethod

class YoutubeHook(
    app: Application,
    lpparam: LoadPackageParam
) : BaseHook(app, lpparam) {

    val ExtensionHook = patch {
        injectHostClassLoaderToSelf(this::class.java.classLoader!!, classLoader)
        DexMethod("$YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE->onCreate(Landroid/os/Bundle;)V").hookMethod {
            before {
                val mainActivity = it.thisObject as Activity
                mainActivity.addModuleAssets()
                Utils.setContext(mainActivity)
            }
        }

        ExtensionResourceHook.run(this)
    }

    override val patches = arrayOf(
        ExtensionHook,
        VideoAds,
        BackgroundPlayback,
        SanitizeSharingLinks,
        HideAds,
        SponsorBlock,
        CopyVideoUrl,
        Downloads,
        HideShortsComponents,
        NavigationButtons,
        SwipeControls,
        VideoQuality,
        DisableResumingShortsOnStartup,
        HideLayoutComponents,
        HideButtons,
        PlaybackSpeed,
        EnableDebugging,
        ForceOriginalAudio,
        DisableVideoCodecs,
        CheckRecycleBitmapMediaSession,
        // make sure settingsHook at end to build preferences
        SettingsHook
    )

}
