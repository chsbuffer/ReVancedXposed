package io.github.chsbuffer.revancedxposed.youtube.video.codecs

import app.revanced.extension.youtube.patches.DisableVideoCodecsPatch
import app.revanced.extension.youtube.settings.Settings
import io.github.chsbuffer.revancedxposed.invokeOriginalMethod
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.SwitchPreference
import io.github.chsbuffer.revancedxposed.youtube.YoutubeHook
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen
import org.luckypray.dexkit.wrap.DexMethod

fun YoutubeHook.DisableVideoCodecs() {
    PreferenceScreen.VIDEO.addPreferences(
        SwitchPreference("revanced_disable_hdr_video"),
        SwitchPreference("revanced_force_avc_codec")
    )

    DexMethod("Landroid/view/Display\$HdrCapabilities;->getSupportedHdrTypes()[I").hookMethod {
        before {
            it.result = if (Settings.DISABLE_HDR_VIDEO.get())
                IntArray(0)
            else
                it.invokeOriginalMethod()
        }
    }

    ::vp9CapabilityFingerprint.hookMethod {
        before {
            if (!DisableVideoCodecsPatch.allowVP9()) {
                it.result = false
            }
        }
    }
}