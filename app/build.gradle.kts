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

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.navigation:navigation-fragment:2.7.5")
    implementation("androidx.navigation:navigation-ui:2.7.5")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Material3追加
    implementation("androidx.compose.material3:material3:1.2.1")


    // 下記は公式より
    // kapt, ksp は公式通りプラグイン等を変更したがエラーのままで機能しなかった
    // https://developer.android.com/jetpack/androidx/releases/room#declaring_dependencies
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    // To use Kotlin annotation processing tool (kapt)
    //kapt("androidx.room:room-compiler:$room_version")
    // To use Kotlin Symbol Processing (KSP)
    //ksp("androidx.room:room-compiler:$room_version")
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
    implementation("androidx.preference:preference:1.2.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    // optional - RxJava2 support
    implementation("androidx.datastore:datastore-preferences-rxjava2:1.1.1")
    // optional - RxJava3 support
    implementation("androidx.datastore:datastore-preferences-rxjava3:1.1.1")

    // WorkerManager 追加
    implementation("androidx.work:work-runtime:2.9.0")
}
