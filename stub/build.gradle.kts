plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "stub"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets {
        named("main") {
            java.directories += arrayOf(
                "../revanced-patches/patches/stub/src/main/java",
                "../revanced-patches/extensions/youtube/stub/src/main/java"
            )
        }
    }
}
