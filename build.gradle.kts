// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false

    // RoomのSchemaエクスポート機能を使用する為に追加
    id("androidx.room") version "2.6.1" apply false
}
buildscript {
    dependencies {
        // Navigation SafeArgs 機能追加
        val nav_version = "2.7.7"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
    }
}
