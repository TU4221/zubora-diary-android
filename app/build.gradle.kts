plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt") // MEMO:KotlinプロジェクトにてDataBindingを実装する時、左記プラグイン必要。(警告より)

    // KSP機能追加
    // https://developer.android.com/build/migrate-to-ksp?hl=ja#add-ksp
    id("com.google.devtools.ksp")

    // Hilt機能追加
    // https://developer.android.com/training/dependency-injection/hilt-android?hl=ja#setup
    id("com.google.dagger.hilt.android")

    // Room Schemaエクスポート機能追加
    // https://developer.android.com/training/data-storage/room/migrating-db-versions?hl=ja#export-schemas
    id("androidx.room")

    // Navigation Kotlinシリアル化プラグイン機能追加
    // https://developer.android.com/jetpack/androidx/releases/navigation?hl=JA#declaring_dependencies
    kotlin("plugin.serialization")

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

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Material
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.material3:material3:1.3.1")

    // SplashScreen
    // https://developer.android.com/develop/ui/views/launch/splash-screen?hl=ja#getting-started
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Navigation
    val navVersion = "2.8.8"
    // Jetpack Compose integration
    implementation("androidx.navigation:navigation-compose:$navVersion")
    // Views/Fragments integration
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    // Feature module support for Fragments
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$navVersion")
    // Testing Navigation
    androidTestImplementation("androidx.navigation:navigation-testing:$navVersion")
    // JSON serialization library, works with the Kotlin serialization plugin
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Lifecycle
    // https://developer.android.com/jetpack/androidx/releases/lifecycle?hl=ja#kotlin
    val lifecycleVersion = "2.8.7"
    val archVersion = "2.2.0"
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    // ViewModel utilities for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    // Lifecycles only (without ViewModel or LiveData)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    // Lifecycle utilities for Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    // Saved state module for ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion")
    // Annotation processor
    // MEMO:"androidx.lifecycle:lifecycle-common-java8"を使用する為、下記コメントアウト。
    //ksp("androidx.lifecycle:lifecycle-compiler:$lifecycleVersion")
    // alternately - if using Java8, use the following instead of lifecycle-compiler
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
    // optional - helpers for implementing LifecycleOwner in a Service
    implementation("androidx.lifecycle:lifecycle-service:$lifecycleVersion")
    // optional - ProcessLifecycleOwner provides a lifecycle for the whole application process
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")
    // optional - ReactiveStreams support for LiveData
    implementation("androidx.lifecycle:lifecycle-reactivestreams-ktx:$lifecycleVersion")
    // optional - Test helpers for LiveData
    testImplementation("androidx.arch.core:core-testing:$archVersion")
    // optional - Test helpers for Lifecycle runtime
    testImplementation ("androidx.lifecycle:lifecycle-runtime-testing:$lifecycleVersion")

    // Room(SQLiteデータベース管理)
    // https://developer.android.com/jetpack/androidx/releases/room#declaring_dependencies
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    // See Add the KSP plugin to your project
    ksp("androidx.room:room-compiler:$roomVersion")
    // If this project only uses Java source, use the Java annotationProcessor
    // No additional plugins are necessary
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")
    // optional - RxJava2 support for Room
    implementation("androidx.room:room-rxjava2:$roomVersion")
    // optional - RxJava3 support for Room
    implementation("androidx.room:room-rxjava3:$roomVersion")
    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation("androidx.room:room-guava:$roomVersion")
    // optional - Test helpers
    testImplementation("androidx.room:room-testing:$roomVersion")
    // optional - Paging 3 Integration
    implementation("androidx.room:room-paging:$roomVersion")

    // PreferencesDataStore
    // https://developer.android.com/topic/libraries/architecture/datastore?hl=ja#preferences-datastore-dependencies
    implementation("androidx.datastore:datastore-preferences:1.1.3")
    // optional - RxJava2 support
    implementation("androidx.datastore:datastore-preferences-rxjava2:1.1.3")
    // optional - RxJava3 support
    implementation("androidx.datastore:datastore-preferences-rxjava3:1.1.3")

    // WorkerManager(バックグラウンドタスク管理)
    // https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started?hl=ja
    val workVersion = "2.10.0"
    // (Java only)
    implementation("androidx.work:work-runtime:$workVersion")
    // Kotlin + coroutines
    implementation("androidx.work:work-runtime-ktx:$workVersion")
    // optional - RxJava2 support
    implementation("androidx.work:work-rxjava2:$workVersion")
    // optional - GCMNetworkManager support
    implementation("androidx.work:work-gcm:$workVersion")
    // optional - Test helpers
    androidTestImplementation("androidx.work:work-testing:$workVersion")
    // optional - Multi process support
    implementation("androidx.work:work-multiprocess:$workVersion")

    // Retrofit(HTTP通信)
    // https://square.github.io/retrofit/
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    // FusedLocationProviderClient(位置情報)
    // https://developer.android.com/develop/sensors-and-location/location/retrieve-current?hl=ja#setup
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Hilt(依存性注入)
    // https://developer.android.com/training/dependency-injection/hilt-android?hl=ja#setup
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")

    // HiltNavigation
    // https://developer.android.com/training/dependency-injection/hilt-jetpack?hl=ja#viewmodel-navigation
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")

    // HiltWorker
    // https://developer.android.com/training/dependency-injection/hilt-jetpack?hl=ja#workmanager
    implementation("androidx.hilt:hilt-work:1.2.0")
    // When using Kotlin.
    ksp("androidx.hilt:hilt-compiler:1.2.0") // MEMO:Javaを使用していても左記未設定だとWorkerの引数を変更する事はできない。
    // When using Java.
    annotationProcessor("androidx.hilt:hilt-compiler:1.2.0")

    // kizitonwose/Calendar
    // https://github.com/kizitonwose/Calendar
    val desugarVersion = "2.1.5"
    val calendarVersion = "2.5.0"
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:$desugarVersion")
    // The view calendar library
    implementation("com.kizitonwose.calendar:view:$calendarVersion")
    // The compose calendar library
    implementation("com.kizitonwose.calendar:compose:$calendarVersion")

    // LeakCanary
    // https://square.github.io/leakcanary/getting_started/
    // debugImplementation because LeakCanary should only run in debug builds.
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
}

// Room Schemaエクスポート機能追加
// https://developer.android.com/training/data-storage/room/migrating-db-versions?hl=ja#export-schemas
room {
    schemaDirectory("$projectDir/schemas")
}
