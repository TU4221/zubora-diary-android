

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android)  apply false
    alias(libs.plugins.kotlin.compose) apply false

    // KSP機能追加
    // https://developer.android.com/build/migrate-to-ksp?hl=ja#add-ksp
    alias(libs.plugins.devtools.ksp) apply false

    // Hilt機能追加
    // https://developer.android.com/training/dependency-injection/hilt-android?hl=ja#setup
    alias(libs.plugins.hilt.android) apply false

    // Room Schemaエクスポート機能追加
    // https://developer.android.com/training/data-storage/room/migrating-db-versions?hl=ja#export-schemas
    alias(libs.plugins.androidx.room) apply false

    // Navigation Kotlinシリアル化プラグイン機能追加
    // https://developer.android.com/jetpack/androidx/releases/navigation?hl=JA#declaring_dependencies
    alias(libs.plugins.kotlin.serialization) apply false

    // AboutLibraries機能追加
    alias(libs.plugins.aboutLibraries) apply false
}
buildscript {
    dependencies {
        // Navigation SafeArgs 機能追加
        // https://developer.android.com/jetpack/androidx/releases/navigation?hl=JA#safe_args
        val navVersion = "2.8.8"
        classpath(libs.androidx.navigation.safe.args.gradle.plugin)
        //classpath(libs.androidx.navigation.safe.args.gradle.plugin)
    }
}
