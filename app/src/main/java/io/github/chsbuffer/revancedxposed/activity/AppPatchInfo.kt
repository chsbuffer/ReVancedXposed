package io.github.chsbuffer.revancedxposed.activity

import io.github.chsbuffer.revancedxposed.Patch
import io.github.chsbuffer.revancedxposed.googlephotos.GooglePhotosPatches
import io.github.chsbuffer.revancedxposed.meta.MetaPatches
import io.github.chsbuffer.revancedxposed.music.YTMusicPatches
import io.github.chsbuffer.revancedxposed.photomath.PhotomathPatches
import io.github.chsbuffer.revancedxposed.reddit.RedditPatches
import io.github.chsbuffer.revancedxposed.strava.StravaPatches
import io.github.chsbuffer.revancedxposed.youtube.YouTubePatches

class AppPatchInfo(val appName: String, val packageName: String, val patches: Array<Patch>)

val appPatchConfigurations = listOf(
    AppPatchInfo("YouTube", "com.google.android.youtube", YouTubePatches),
    AppPatchInfo("YT Music", "com.google.android.apps.youtube.music", YTMusicPatches),
    AppPatchInfo("Reddit (2024.17.0)", "com.reddit.frontpage", RedditPatches),
    AppPatchInfo("Google Photos", "com.google.android.apps.photos", GooglePhotosPatches),
    AppPatchInfo("Instagram", "com.instagram.android", MetaPatches),
    AppPatchInfo("Threads", "com.instagram.barcelona", MetaPatches),
    AppPatchInfo("Strava", "com.strava", StravaPatches),
    AppPatchInfo("Photomath", "com.microblink.photomath", PhotomathPatches),
)
