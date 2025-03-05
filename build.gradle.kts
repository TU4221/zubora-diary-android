

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.8.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false

    // KSP機能追加
    // https://developer.android.com/build/migrate-to-ksp?hl=ja#add-ksp
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false

    // Hilt機能追加
    // https://developer.android.com/training/dependency-injection/hilt-android?hl=ja#setup
    id("com.google.dagger.hilt.android") version "2.51.1" apply false

    // Room Schemaエクスポート機能追加
    // https://developer.android.com/training/data-storage/room/migrating-db-versions?hl=ja#export-schemas
    id("androidx.room") version "2.6.1" apply false

    // Navigation Kotlinシリアル化プラグイン機能追加
    // https://developer.android.com/jetpack/androidx/releases/navigation?hl=JA#declaring_dependencies
    kotlin("plugin.serialization") version "2.0.21" apply false
}
buildscript {
    dependencies {
        // Navigation SafeArgs 機能追加
        // https://developer.android.com/jetpack/androidx/releases/navigation?hl=JA#safe_args
        val navVersion = "2.8.8"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$navVersion")
    }
}
