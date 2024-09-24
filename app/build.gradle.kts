import com.android.build.gradle.tasks.KSP_PROCESSORS_INDICATOR_FILE

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    // Navigation SafeArgs 機能追加
    // MEMO:下記追加同期後、Gradleの他の箇所を変更してから同期するとDirectionsクラスがエラーになる。(Buildは通る)
    //      下記削除同期後、再度追加同期を行えばエラーは消える。
    //      この解決策を発見する前にキャッシュクリア、再起動を行ったが関係なさそう。
    id("androidx.navigation.safeargs")

    // RoomのSchemaエクスポート機能を使用する為に追加
    id("androidx.room")

    // Hilt機能追加
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")

    // KSP機能追加
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.websarva.wings.android.zuboradiary"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.websarva.wings.android.zuboradiary"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // kizitonwose/Calendar 追加設定
        // Required ONLY if your minSdkVersion is below 21
        multiDexEnabled = true
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

        // kizitonwose/Calendar 追加設定
        // Enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true
        // Set Java compatibility (version can be higher if desired)
        //sourceCompatibility JavaVersion.VERSION_1_8 // 前の行で記述済みの為、コメントアウト。(記録として残す)
        //targetCompatibility JavaVersion.VERSION_1_8 // 前の行で記述済みの為、コメントアウト。(記録として残す)

    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    // kizitonwose/Calendar 追加設定
    // HACK:kotlinOptionsの設定ができない為保留。後日調べる。
    kotlinOptions {
        // Also add this for Kotlin projects (version can be higher if desired)
        jvmTarget = "1.8"
    }

    // RoomのSchemaエクスポート機能を使用する為に追加
    room {
        schemaDirectory("$projectDir/schemas")
    }
}


dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Material3追加
    implementation("androidx.compose.material3:material3:1.3.0")


    // 下記は公式より
    // kapt, ksp は公式通りプラグイン等を変更したがエラーのままで機能しなかった
    // https://developer.android.com/jetpack/androidx/releases/room#declaring_dependencies
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    // To use Kotlin annotation processing tool (kapt)
    kapt("androidx.room:room-compiler:$room_version") // kapt -> kspに置換(例外発生)
    // To use Kotlin Symbol Processing (KSP)
    // ksp("androidx.room:room-compiler:$room_version")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")
    // optional - RxJava2 support for Room
    implementation("androidx.room:room-rxjava2:$room_version")
    // optional - RxJava3 support for Room
    implementation("androidx.room:room-rxjava3:$room_version")
    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation("androidx.room:room-guava:$room_version")
    // optional - Test helpers
    testImplementation("androidx.room:room-testing:$room_version")
    // optional - Paging 3 Integration
    implementation("androidx.room:room-paging:$room_version")


    // zennの記事より上記不足分追加
    // https://codezine.jp/article/detail/17124?p=1&anchor=0
    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation("com.google.guava:guava:31.1-android")


    // kizitonwose/Calendar 追加設定
    val desugar_version = "2.0.4"
    val calendar_version = "2.5.0"
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:$desugar_version")
    // The view calendar library
    implementation("com.kizitonwose.calendar:view:$calendar_version")
    // The compose calendar library
    implementation("com.kizitonwose.calendar:compose:$calendar_version")


    // Preference,PreferencesDataStore 追加
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    // optional - RxJava2 support
    implementation("androidx.datastore:datastore-preferences-rxjava2:1.1.1")
    // optional - RxJava3 support
    implementation("androidx.datastore:datastore-preferences-rxjava3:1.1.1")

    // WorkerManager 追加
    val work_version = "2.9.0"
    // (Java only)
    implementation("androidx.work:work-runtime:$work_version")
    // Kotlin + coroutines
    implementation("androidx.work:work-runtime-ktx:$work_version")
    // optional - RxJava2 support
    implementation("androidx.work:work-rxjava2:$work_version")
    // optional - GCMNetworkManager support
    implementation("androidx.work:work-gcm:$work_version")
    // optional - Test helpers
    androidTestImplementation("androidx.work:work-testing:$work_version")
    // optional - Multiprocess support
    implementation("androidx.work:work-multiprocess:$work_version")

    // Retrofit 追加
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    //  FusedLocationProviderClient(位置情報利用) 追加
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // ライフサイクル管理 追加
    // TODO:kotlinとJavaで切り換えること
    val lifecycle_version = "2.8.4"
    val arch_version = "2.2.0"
    // kotlin
    /*
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    // ViewModel utilities for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    // Lifecycles only (without ViewModel or LiveData)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
    // Lifecycle utilities for Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycle_version")
    // Saved state module for ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycle_version")
    // Annotation processor
    kapt("androidx.lifecycle:lifecycle-compiler:$lifecycle_version")
    // alternately - if using Java8, use the following instead of lifecycle-compiler
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycle_version")
    // optional - helpers for implementing LifecycleOwner in a Service
    implementation("androidx.lifecycle:lifecycle-service:$lifecycle_version")
    // optional - ProcessLifecycleOwner provides a lifecycle for the whole application process
    implementation("androidx.lifecycle:lifecycle-process:$lifecycle_version")
    // optional - ReactiveStreams support for LiveData
    implementation("androidx.lifecycle:lifecycle-reactivestreams-ktx:$lifecycle_version")
    // optional - Test helpers for LiveData
    testImplementation("androidx.arch.core:core-testing:$arch_version")
    // optional - Test helpers for Lifecycle runtime
    testImplementation ("androidx.lifecycle:lifecycle-runtime-testing:$lifecycle_version")
    */

    //Java
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel:$lifecycle_version")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata:$lifecycle_version")
    // Lifecycles only (without ViewModel or LiveData)
    implementation("androidx.lifecycle:lifecycle-runtime:$lifecycle_version")
    // Saved state module for ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycle_version")
    // Annotation processor
    annotationProcessor("androidx.lifecycle:lifecycle-compiler:$lifecycle_version")
    // alternately - if using Java8, use the following instead of lifecycle-compiler
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycle_version")
    // optional - helpers for implementing LifecycleOwner in a Service
    implementation("androidx.lifecycle:lifecycle-service:$lifecycle_version")
    // optional - ProcessLifecycleOwner provides a lifecycle for the whole application process
    implementation("androidx.lifecycle:lifecycle-process:$lifecycle_version")
    // optional - ReactiveStreams support for LiveData
    implementation("androidx.lifecycle:lifecycle-reactivestreams:$lifecycle_version")
    // optional - Test helpers for LiveData
    testImplementation("androidx.arch.core:core-testing:$arch_version")
    // optional - Test helpers for Lifecycle runtime
    testImplementation("androidx.lifecycle:lifecycle-runtime-testing:$lifecycle_version")

    // Hilt機能追加
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    // HiltLifecycle
    //implementation("androidx.hilt.lifecycle:1.2.0")
    // HiltNavigation
    //implementation("androidx.hilt:hilt-navigation:1.2.0")
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")
    // HiltWorker
    implementation("androidx.hilt:hilt-work:1.2.0")
    // When using Kotlin.
    kapt("androidx.hilt:hilt-compiler:1.2.0") // MEMO:Javaを使用していても左記未設定だとWorkerの引数を変更する事はできない。
    // When using Java.
    annotationProcessor("androidx.hilt:hilt-compiler:1.2.0")
}

// Hilt機能追加(生成されたコードへの参照を許可する)
kapt {
    correctErrorTypes = true
}
