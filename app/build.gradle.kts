plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt") // MEMO:KotlinプロジェクトにてDataBindingを実装する時、左記プラグイン必要。(警告より)

    // KSP機能追加
    // https://developer.android.com/build/migrate-to-ksp?hl=ja#add-ksp
    alias(libs.plugins.devtools.ksp)

    // Hilt機能追加
    // https://developer.android.com/training/dependency-injection/hilt-android?hl=ja#setup
    alias(libs.plugins.hilt.android)

    // Room Schemaエクスポート機能追加
    // https://developer.android.com/training/data-storage/room/migrating-db-versions?hl=ja#export-schemas
    alias(libs.plugins.androidx.room)

    // Navigation Kotlinシリアル化プラグイン機能追加
    // https://developer.android.com/jetpack/androidx/releases/navigation?hl=JA#declaring_dependencies
    alias(libs.plugins.kotlin.serialization)

    // Navigation SafeArgs 機能追加
    // https://developer.android.com/jetpack/androidx/releases/navigation?hl=JA#safe_args
    id("androidx.navigation.safeargs")

    // AboutLibraries機能追加
    alias(libs.plugins.aboutLibraries)

    // カスタムクラスのSavedStateHandle対応機能追加
    id("kotlin-parcelize")
}

android {
    namespace = "com.websarva.wings.android.zuboradiary"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.websarva.wings.android.zuboradiary"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        // kizitonwose/Calendar
        // https://github.com/kizitonwose/Calendar
        // Enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
        compose = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.play.services.contextmanager)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Compose
    // https://developer.android.com/develop/ui/compose/setup?hl=ja#setup-compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    // Android Studio Preview support
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    // UI Tests
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Material
    implementation(libs.material)
    implementation(libs.androidx.material3)

    // SplashScreen
    // https://developer.android.com/develop/ui/views/launch/splash-screen?hl=ja#getting-started
    implementation(libs.androidx.core.splashscreen)

    // Navigation
    // https://developer.android.com/guide/navigation?hl=ja#set-up
    // Jetpack Compose integration
    implementation(libs.androidx.navigation.compose)
    // Views/Fragments integration
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    // Feature module support for Fragments
    implementation(libs.androidx.navigation.dynamic.features.fragment)
    // Testing Navigation
    androidTestImplementation(libs.androidx.navigation.testing)
    // JSON serialization library, works with the Kotlin serialization plugin
    implementation(libs.kotlinx.serialization.json)

    // Lifecycle
    // https://developer.android.com/jetpack/androidx/releases/lifecycle?hl=ja#kotlin
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // ViewModel utilities for Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Lifecycles only (without ViewModel or LiveData)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // Lifecycle utilities for Compose
    implementation(libs.androidx.lifecycle.runtime.compose)
    // Saved state module for ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    // Annotation processor
    ksp(libs.androidx.lifecycle.compiler)
    // optional - Test helpers for Lifecycle runtime
    testImplementation (libs.androidx.lifecycle.runtime.testing)

    // Room(SQLiteデータベース管理)
    // https://developer.android.com/jetpack/androidx/releases/room#declaring_dependencies
    implementation(libs.androidx.room.runtime)
    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    // See Add the KSP plugin to your project
    ksp(libs.androidx.room.compiler)
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)
    // optional - Test helpers
    testImplementation(libs.androidx.room.testing)

    // PreferencesDataStore
    // https://developer.android.com/topic/libraries/architecture/datastore?hl=ja#preferences-datastore-dependencies
    implementation(libs.androidx.datastore.preferences)

    // WorkerManager(バックグラウンドタスク管理)
    // https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started?hl=ja
    // Kotlin + coroutines
    implementation(libs.androidx.work.runtime.ktx)
    // optional - Test helpers
    androidTestImplementation(libs.androidx.work.testing)

    // Retrofit(HTTP通信)
    // https://square.github.io/retrofit/
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.moshi.kotlin)
    implementation(libs.converter.moshi)

    // FusedLocationProviderClient(位置情報)
    // https://developer.android.com/develop/sensors-and-location/location/retrieve-current?hl=ja#setup
    implementation(libs.play.services.location)

    // Hilt(依存性注入)
    // https://developer.android.com/training/dependency-injection/hilt-android?hl=ja#setup
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // HiltNavigation
    // https://developer.android.com/training/dependency-injection/hilt-jetpack?hl=ja#viewmodel-navigation
    implementation(libs.androidx.hilt.navigation.fragment)

    // HiltWorker
    // https://developer.android.com/training/dependency-injection/hilt-jetpack?hl=ja#workmanager
    implementation(libs.androidx.hilt.work)
    // When using Kotlin.
    ksp(libs.androidx.hilt.compiler)

    // kizitonwose/Calendar
    // https://github.com/kizitonwose/Calendar
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    // The view calendar library
    implementation(libs.kizitonwose.calendar.view)
    // The compose calendar library
    implementation(libs.kizitonwose.calendar.compose)

    // Coil 3 (For Android Views) (画像表示)
    //implementation(libs.coil)

    // AboutLibraries
    // https://github.com/mikepenz/AboutLibraries
    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose.core)
    implementation(libs.aboutlibraries.compose.m3) // material 3
    implementation(libs.aboutlibraries.compose.view)

    // LeakCanary
    // https://square.github.io/leakcanary/getting_started/
    // debugImplementation because LeakCanary should only run in debug builds.
    debugImplementation(libs.leakcanary.android)
}

// Room Schemaエクスポート機能追加
// https://developer.android.com/training/data-storage/room/migrating-db-versions?hl=ja#export-schemas
room {
    schemaDirectory("$projectDir/schemas")
}

// AboutLibraries Libraries手動追加
aboutLibraries {
    configPath = "$projectDir/src/main/assets/about_libraries"
}
