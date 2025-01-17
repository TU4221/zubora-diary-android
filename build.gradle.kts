

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false

    // RoomのSchemaエクスポート機能を使用する為に追加
    id("androidx.room") version "2.6.1" apply false

    // Hilt機能追加
    id("com.google.dagger.hilt.android") version "2.51.1" apply false

    // KSP機能追加
    // https://developer.android.com/build/migrate-to-ksp?hl=ja
    id("com.google.devtools.ksp") version "1.8.10-1.0.9" apply false // kotlin Ver.1.8.10
}
buildscript {
    dependencies {
        // Navigation SafeArgs 機能追加
        val nav_version = "2.8.5"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
    }
}
