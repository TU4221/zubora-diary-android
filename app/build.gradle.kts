plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

        // kizitonwose/Calendar
        // https://github.com/kizitonwose/Calendar
        // Enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

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
    // LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)
    // Lifecycles only (without ViewModel or LiveData)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // Lifecycle utilities for Compose
    implementation(libs.androidx.lifecycle.runtime.compose)
    // Saved state module for ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    // Annotation processor
    // MEMO:"androidx.lifecycle:lifecycle-common-java8"を使用する為、下記コメントアウト。
    //ksp("androidx.lifecycle:lifecycle-compiler:$lifecycleVersion")
    // alternately - if using Java8, use the following instead of lifecycle-compiler
    implementation(libs.androidx.lifecycle.common.java8)
    // optional - helpers for implementing LifecycleOwner in a Service
    implementation(libs.androidx.lifecycle.service)
    // optional - ProcessLifecycleOwner provides a lifecycle for the whole application process
    implementation(libs.androidx.lifecycle.process)
    // optional - ReactiveStreams support for LiveData
    implementation(libs.androidx.lifecycle.reactivestreams.ktx)
    // optional - Test helpers for LiveData
    testImplementation(libs.androidx.core.testing)
    // optional - Test helpers for Lifecycle runtime
    testImplementation (libs.androidx.lifecycle.runtime.testing)

    // Room(SQLiteデータベース管理)
    // https://developer.android.com/jetpack/androidx/releases/room#declaring_dependencies
    implementation(libs.androidx.room.runtime)
    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    // See Add the KSP plugin to your project
    ksp(libs.androidx.room.compiler)
    // If this project only uses Java source, use the Java annotationProcessor
    // No additional plugins are necessary
    annotationProcessor(libs.androidx.room.compiler)
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)
    // optional - RxJava2 support for Room
    implementation(libs.androidx.room.rxjava2)
    // optional - RxJava3 support for Room
    implementation(libs.androidx.room.rxjava3)
    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation(libs.androidx.room.guava)
    // optional - Test helpers
    testImplementation(libs.androidx.room.testing)
    // optional - Paging 3 Integration
    implementation(libs.androidx.room.paging)

    // PreferencesDataStore
    // https://developer.android.com/topic/libraries/architecture/datastore?hl=ja#preferences-datastore-dependencies
    implementation(libs.androidx.datastore.preferences)
    // optional - RxJava2 support
    implementation(libs.androidx.datastore.preferences.rxjava2)
    // optional - RxJava3 support
    implementation(libs.androidx.datastore.preferences.rxjava3)

    // WorkerManager(バックグラウンドタスク管理)
    // https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started?hl=ja
    // (Java only)
    implementation(libs.androidx.work.runtime)
    // Kotlin + coroutines
    implementation(libs.androidx.work.runtime.ktx)
    // optional - RxJava2 support
    implementation(libs.androidx.work.rxjava2)
    // optional - GCMNetworkManager support
    implementation(libs.androidx.work.gcm)
    // optional - Test helpers
    androidTestImplementation(libs.androidx.work.testing)
    // optional - Multi process support
    implementation(libs.androidx.work.multiprocess)

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
    ksp(libs.androidx.hilt.compiler) // MEMO:Javaを使用していても左記未設定だとWorkerの引数を変更する事はできない。
    // When using Java.
    annotationProcessor(libs.androidx.hilt.compiler)

    // kizitonwose/Calendar
    // https://github.com/kizitonwose/Calendar
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    // The view calendar library
    implementation(libs.kizitonwose.calendar.view)
    // The compose calendar library
    implementation(libs.kizitonwose.calendar.compose)

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
