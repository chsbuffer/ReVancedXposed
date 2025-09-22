package io.github.chsbuffer.revancedxposed.music

import android.app.Application
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.chsbuffer.revancedxposed.BaseHook
import io.github.chsbuffer.revancedxposed.music.ad.video.HideVideoAds
import io.github.chsbuffer.revancedxposed.music.audio.exclusiveaudio.EnableExclusiveAudioPlayback
import io.github.chsbuffer.revancedxposed.music.layout.premium.HideGetPremium
import io.github.chsbuffer.revancedxposed.music.layout.upgradebutton.HideUpgradeButton
import io.github.chsbuffer.revancedxposed.music.misc.backgroundplayback.BackgroundPlayback
import io.github.chsbuffer.revancedxposed.music.misc.settings.SettingsHook
import io.github.chsbuffer.revancedxposed.shared.misc.CheckRecycleBitmapMediaSession

class MusicHook(app: Application, lpparam: LoadPackageParam) : BaseHook(app, lpparam) {
    override val hooks = arrayOf(
        ::ExtensionResourceHook,
        ::HideVideoAds,
        ::BackgroundPlayback,
        ::HideUpgradeButton,
        ::HideGetPremium,
        ::EnableExclusiveAudioPlayback,
        ::CheckRecycleBitmapMediaSession,
        ::SettingsHook
    )
}