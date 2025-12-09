package io.github.chsbuffer.revancedxposed.shared.misc

import android.media.MediaMetadata
import android.media.session.MediaSession
import android.os.Build
import de.robv.android.xposed.XposedBridge
import io.github.chsbuffer.revancedxposed.callStaticMethod
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.hookMethod
import org.luckypray.dexkit.wrap.DexMethod
import java.lang.reflect.Member

val CheckRecycleBitmapMediaSession = patch(name = "<CheckRecycleBitmapMediaSession>") {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return@patch

    if (runCatching { DexMethod("Landroid/media/MediaMetadata\$Builder;->calculateSampleSize(IIII)I").toMember() }.isSuccess) {
        XposedBridge.log("Applying MediaMetadata recycle Bitmap workaround (https://github.com/chsbuffer/ReVancedXposed/issues/29#issuecomment-3084170279)")

        try {
            MediaMetadata.Builder::class.java.getMethod("build").hookMethod {
                before {
                    (it.thisObject as MediaMetadata.Builder).setBitmapDimensionLimit(Integer.MAX_VALUE)
                }
            }

            val setMetadata =
                MediaSession::class.java.getMethod("setMetadata", MediaMetadata::class.java)
            XposedBridge::class.java.callStaticMethod("deoptimizeMethod", setMetadata as Member)
        } catch (err: Throwable) {
            XposedBridge.log(err)
        }
    }
}
