package io.github.chsbuffer.revancedxposed.youtube.misc.recyclerviewtree.hook

import android.support.v7.widget.RecyclerView
import io.github.chsbuffer.revancedxposed.patch
import io.github.chsbuffer.revancedxposed.scopedHook

val addRecyclerViewTreeHook = mutableListOf<(RecyclerView) -> Unit>()

val recyclerViewTreeHook = patch {
    ::recyclerViewTreeObserverFingerprint.hookMethod(scopedHook(::RecyclerView_addOnScrollListener.member) {
        before {
            val recyclerView = it.thisObject as RecyclerView
            addRecyclerViewTreeHook.forEach { hook ->
                hook(recyclerView)
            }
        }
    })
}