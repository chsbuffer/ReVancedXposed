package io.github.chsbuffer.revancedxposed.youtube.misc.playertype

import android.annotation.SuppressLint
import android.view.View
import app.revanced.extension.youtube.patches.PlayerTypeHookPatch
import io.github.chsbuffer.revancedxposed.patch

@SuppressLint("NonUniqueDexKitData")
val PlayerTypeHook = patch(
    description = "Hook to get the current player type and video playback state.",
) {
    ::playerTypeFingerprint.hookMethod {
        before { param ->
            PlayerTypeHookPatch.setPlayerType(param.args[0] as Enum<*>)
        }
    }

    ::reelWatchPagerFingerprint.hookMethod {
        val field = ::ReelPlayerViewField.field
        after { param ->
            val thiz = param.thisObject
            val view = field.get(thiz) as View
            PlayerTypeHookPatch.onShortsCreate(view)
        }
    }

    ::videoStateFingerprint.hookMethod {
        val field = ::videoStateParameterField.field
        before { param ->
            PlayerTypeHookPatch.setVideoState(field.get(param.args[0]) as Enum<*>)
        }
    }
}