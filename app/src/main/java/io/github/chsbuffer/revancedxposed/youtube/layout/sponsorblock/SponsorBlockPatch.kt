package io.github.chsbuffer.revancedxposed.youtube.layout.sponsorblock

import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.view.ViewGroup
import app.revanced.extension.shared.Utils
import app.revanced.extension.youtube.sponsorblock.SegmentPlaybackController
import app.revanced.extension.youtube.sponsorblock.ui.CreateSegmentButton
import app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockAboutPreference
import app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockPreferenceGroup
import app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockStatsPreferenceCategory
import app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockViewController
import app.revanced.extension.youtube.sponsorblock.ui.VotingButton
import io.github.chsbuffer.revancedxposed.R
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.scopedHook
import io.github.chsbuffer.revancedxposed.setObjectField
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.NonInteractivePreference
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.PreferenceCategory
import io.github.chsbuffer.revancedxposed.shared.misc.settings.preference.PreferenceScreenPreference
import io.github.chsbuffer.revancedxposed.youtube.misc.playercontrols.ControlInitializer
import io.github.chsbuffer.revancedxposed.youtube.misc.playercontrols.PlayerControls
import io.github.chsbuffer.revancedxposed.youtube.misc.playercontrols.addTopControl
import io.github.chsbuffer.revancedxposed.youtube.misc.playercontrols.initializeTopControl
import io.github.chsbuffer.revancedxposed.youtube.misc.playertype.PlayerTypeHook
import io.github.chsbuffer.revancedxposed.youtube.misc.settings.PreferenceScreen
import io.github.chsbuffer.revancedxposed.youtube.video.information.VideoInformationPatch
import io.github.chsbuffer.revancedxposed.youtube.video.information.onCreateHook
import io.github.chsbuffer.revancedxposed.youtube.video.information.videoTimeHooks
import io.github.chsbuffer.revancedxposed.youtube.video.videoid.VideoId
import io.github.chsbuffer.revancedxposed.youtube.video.videoid.videoIdHooks
import org.luckypray.dexkit.wrap.DexMethod

val SponsorBlock = patch(
    name = "SponsorBlock",
    description = "Adds options to enable and configure SponsorBlock, which can skip undesired video segments such as sponsored content.",
) {
    dependsOn(
        VideoInformationPatch,
        VideoId,
        PlayerTypeHook,
        PlayerControls,
    )

    PreferenceScreen.SPONSORBLOCK.addPreferences(
        // SB setting is old code with lots of custom preferences and updating behavior.
        // Added as a preference group and not a fragment so the preferences are searchable.
        PreferenceCategory(
            key = "revanced_settings_screen_10_sponsorblock",
            sorting = PreferenceScreenPreference.Sorting.UNSORTED,
            preferences = emptySet(), // Preferences are added by custom class at runtime.
            tag = SponsorBlockPreferenceGroup::class.java
        ), PreferenceCategory(
            key = "revanced_sb_stats",
            sorting = PreferenceScreenPreference.Sorting.UNSORTED,
            preferences = emptySet(), // Preferences are added by custom class at runtime.
            tag = SponsorBlockStatsPreferenceCategory::class.java
        ), PreferenceCategory(
            key = "revanced_sb_about",
            sorting = PreferenceScreenPreference.Sorting.UNSORTED,
            preferences = setOf(
                NonInteractivePreference(
                    key = "revanced_sb_about_api",
                    tag = SponsorBlockAboutPreference::class.java,
                    selectable = true,
                )
            )
        )
    )

    addTopControl(R.layout.revanced_sb_button)

    // Hook the video time methods.
    videoTimeHooks.add { SegmentPlaybackController.setVideoTime(it) }
    videoIdHooks.add { SegmentPlaybackController.setCurrentVideoId(it) }

    // Seekbar drawing
    var rectSetOnce = false
    ::seekbarOnDrawFingerprint.hookMethod {
        val sponsorBarRectField = ::SponsorBarRect.field
        before { param ->
            // Get left and right of seekbar rectangle.
            rectSetOnce = false
            SegmentPlaybackController.setSponsorBarRect(sponsorBarRectField.get(param.thisObject) as Rect)
        }
    }
    val drawCircle =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
            "Landroid/view/DisplayListCanvas;->drawCircle(FFFLandroid/graphics/Paint;)V"
        else
            "Landroid/graphics/RecordingCanvas;->drawCircle(FFFLandroid/graphics/Paint;)V"
    ::seekbarOnDrawFingerprint.hookMethod(
        scopedHook(
            // Set the thickness of the segment.
            DexMethod("Landroid/graphics/Rect;->set(IIII)V").toMethod() to {
                after { param ->
                    // Only the first call to Rect.set from onDraw sets the segment thickness.
                    if (rectSetOnce) return@after
                    SegmentPlaybackController.setSponsorBarThickness((param.thisObject as Rect).height())
                    rectSetOnce = true
                }
            },
            // Find the drawCircle call and draw the segment before it.
            DexMethod(drawCircle).toMethod() to {
                before { param ->
                    SegmentPlaybackController.drawSponsorTimeBars(
                        param.thisObject as Canvas, param.args[1] as Float
                    )
                }
            },
        )
    )

    // Change visibility of the buttons.
    initializeTopControl(
        ControlInitializer(
            R.id.revanced_sb_create_segment_button,
            CreateSegmentButton::initialize,
            CreateSegmentButton::setVisibility,
            CreateSegmentButton::setVisibilityImmediate,
            CreateSegmentButton::setVisibilityNegatedImmediate
        )
    )
    initializeTopControl(
        ControlInitializer(
            R.id.revanced_sb_voting_button,
            VotingButton::initialize,
            VotingButton::setVisibility,
            VotingButton::setVisibilityImmediate,
            VotingButton::setVisibilityNegatedImmediate
        )
    )

    // Append the new time to the player layout.
    ::appendTimeFingerprint.hookMethod {
        before {
            it.args[2] = SegmentPlaybackController.appendTimeWithoutSegments(it.args[2].toString())
        }
    }

    // Initialize the player controller.
    onCreateHook.add { SegmentPlaybackController.initialize(it) }

    // Initialize the SponsorBlock view.
    val controls_overlay_layout =
        Utils.getResourceIdentifier("size_adjustable_youtube_controls_overlay", "layout")
    ::controlsOverlayFingerprint.hookMethod(scopedHook(DexMethod("Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;)Landroid/view/View;").toMember()) {
        val insetOverlayViewLayout = inset_overlay_view_layout
        after { param ->
            if (param.args[0] != controls_overlay_layout) return@after
            val layout = param.result as ViewGroup
            val overlay_view = layout.findViewById<ViewGroup>(insetOverlayViewLayout)
            SponsorBlockViewController.initialize(overlay_view)
        }
    })

    ::adProgressTextViewVisibilityFingerprint.hookMethod(scopedHook(::AdProgressTextVisibility.method) {
        before {
            SegmentPlaybackController.setAdProgressTextVisibility(it.args[0] as Int)
        }
    })

    fun injectClassLoader(self: ClassLoader, host: ClassLoader) {
        host.setObjectField("parent", object : ClassLoader(host.parent) {
            override fun findClass(name: String): Class<*> {
                try {
                    if (name.startsWith("app.revanced")) return self.loadClass(name)
                } catch (_: ClassNotFoundException) {
                }

                throw ClassNotFoundException(name)
            }
        })
    }

    injectClassLoader(this::class.java.classLoader!!, classLoader)
}