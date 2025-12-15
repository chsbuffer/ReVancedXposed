package io.github.chsbuffer.revancedxposed.youtube.misc.playercontrols

import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ImageView
import android.widget.RelativeLayout
import app.revanced.extension.shared.Utils
import app.revanced.extension.youtube.patches.PlayerControlsPatch
import io.github.chsbuffer.revancedxposed.PatchExecutor
import io.github.chsbuffer.revancedxposed.R
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.scopedHook
import org.luckypray.dexkit.wrap.DexMethod

class ControlInitializer(
    val id: Int,
    @JvmField val initializeButton: (controlsView: ViewGroup) -> Unit,
    // visibilityCheckCalls
    @JvmField val setVisibility: (Boolean, Boolean) -> Unit,
    @JvmField val setVisibilityImmediate: (Boolean) -> Unit,
    // Patch works without this hook, but it is needed to use the correct fade out animation
    // duration when tapping the overlay to dismiss.
    @JvmField val setVisibilityNegatedImmediate: () -> Unit
)

private val topControlLayouts = mutableListOf<Int>()
private val bottomControlLayouts = mutableListOf<Int>()
private val topControls = mutableListOf<ControlInitializer>()
private val bottomControls = mutableListOf<ControlInitializer>()

@JvmField
var visibilityImmediateCallbacksExistModified = false

fun onFullscreenButtonVisibilityChanged(isVisible: Boolean) {
    topControls.forEach { it.setVisibilityImmediate(isVisible) }
    bottomControls.forEach { it.setVisibilityImmediate(isVisible) }
//    Logger.printDebug { ("setVisibilityImmediate($isVisible)") }
}

fun addTopControl(layout: Int) {
    topControlLayouts.add(layout)
}

fun addBottomControl(layout: Int) {
    bottomControlLayouts.add(layout)
}

fun initializeTopControl(control: ControlInitializer) {
    topControls.add(control)
    injectVisibilityCheckCall()
}

fun initializeBottomControl(control: ControlInitializer) {
    bottomControls.add(control)
    injectVisibilityCheckCall()
}

private fun injectVisibilityCheckCall() {
    if (!visibilityImmediateCallbacksExistModified) {
        visibilityImmediateCallbacksExistModified = true
    }
}

private fun onTopContainerInflate(viewStub: ViewStub, root: ViewGroup) {
    topControlLayouts.forEach { layout ->
        viewStub.layoutInflater.inflate(layout, root, true)
    }

    val heading = Utils.getChildViewByResourceName<View>(root, "player_video_heading")
    (heading.layoutParams as RelativeLayout.LayoutParams).addRule(
        RelativeLayout.START_OF, R.id.revanced_sb_voting_button
    )

    val revancedSbCreateSegmentButton =
        Utils.getChildViewByResourceName<View?>(root, "revanced_sb_create_segment_button")

    if (revancedSbCreateSegmentButton != null) {
        (revancedSbCreateSegmentButton.layoutParams as RelativeLayout.LayoutParams).addRule(
            RelativeLayout.START_OF, Utils.getResourceIdentifier("music_app_deeplink_button", "id")
        )
    }

    topControls.forEach { control ->
        control.initializeButton(root)
    }
}

private fun onBottomContainerInflate(viewStub: ViewStub, root: ViewGroup) {
    bottomControlLayouts.forEach { layout ->
        viewStub.layoutInflater.inflate(layout, root, true)
    }
    bottomControls.forEach { control ->
        control.initializeButton(root)
    }
}

val PlayerControls = patch(
    description = "Manages the code for the player controls of the YouTube player.",
) {
    DexMethod("Landroid/view/ViewStub;->inflate()Landroid/view/View;").hookMethod {
        after {
            val viewStub = it.thisObject as ViewStub
            val viewStubName = Utils.getContext().resources.getResourceName(viewStub.id)
//            Logger.printDebug { "ViewStub->inflate()" + viewStubName }

            when {
                viewStubName.endsWith("bottom_ui_container_stub") -> {
                    onBottomContainerInflate(viewStub, it.result as ViewGroup)
                }

                viewStubName.endsWith("controls_layout_stub") -> {
                    onTopContainerInflate(viewStub, it.result as ViewGroup)
                }

                else -> return@after
            }
//            Logger.printDebug { "inject into $viewStubName" }
        }
    }

    initInjectVisibilityCheckCall()

    val youtube_controls_bottom_ui_container =
        Utils.getResourceIdentifier("youtube_controls_bottom_ui_container", "id")

    DexMethod("Landroid/support/constraint/ConstraintLayout;->onLayout(ZIIII)V").hookMethod {
        after {
            val controlsView = it.thisObject as ViewGroup
            if (controlsView.id != youtube_controls_bottom_ui_container) return@after

            val fullscreenButton =
                Utils.getChildViewByResourceName<View>(controlsView, "fullscreen_button")
            var rightButton = fullscreenButton

            for (bottomControl in bottomControls) {
                val leftButton = controlsView.findViewById<View>(bottomControl.id)
                if (leftButton.visibility == View.GONE) continue
                // put this button to the left
                leftButton.x = rightButton.x - leftButton.width
                leftButton.y = rightButton.y
                leftButton.layoutParams = leftButton.layoutParams.apply {
                    height = fullscreenButton.height
                }
                rightButton = leftButton
            }
        }
    }
}

private fun PatchExecutor.initInjectVisibilityCheckCall() {
    ::controlsOverlayVisibilityFingerprint.hookMethod {
        before { param ->
            bottomControls.forEach {
                it.setVisibility(param.args[0] as Boolean, param.args[1] as Boolean)
            }
//            Logger.printDebug { "setVisibility(visible: ${param.args[0]}, animated: ${param.args[1]})" }
        }
    }

    // Hook the fullscreen close button.  Used to fix visibility
    // when seeking and other situations.
    ::overlayViewInflateFingerprint.hookMethod(scopedHook(DexMethod("Landroid/view/View;->findViewById(I)Landroid/view/View;").toMember()) {
        val fullscreenButtonId = fullscreen_button_id
        after {
            if (it.args[0] == fullscreenButtonId) {
                PlayerControlsPatch.setFullscreenCloseButton(it.result as ImageView)
            }
        }
    })

    //
    ::motionEventFingerprint.hookMethod(scopedHook(DexMethod("Landroid/view/View;->setTranslationY(F)V").toMethod()) {
        after {
            // FIXME Animation lags behind
            bottomControls.forEach { it.setVisibilityNegatedImmediate() }
//            Logger.printDebug { "setVisibilityNegatedImmediate()" }
        }
    })
}