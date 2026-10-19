plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

val releaseKeystore: String? = System.getenv("VP_KEYSTORE_PATH")

android {
    namespace = "com.vitapulse.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vitapulse.app"
        minSdk = 34          // Android 14
        targetSdk = 35       // runs on Android 15, 16 and later
        // CI run number → every build installs as an update over the previous one.
        val build = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1
        versionCode = build
        versionName = "1.1.$build"
    }

    // One APK per CPU architecture: each phone only gets the native code it needs.
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a")
            isUniversalApk = false
        }
    }

    signingConfigs {
        create("release") {
            if (releaseKeystore != null) {
                storeFile = file(releaseKeystore)
                storePassword = System.getenv("VP_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("VP_KEY_ALIAS")
                keyPassword = System.getenv("VP_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Without your own keystore the release build is signed with the debug key (testing only).
            signingConfig = if (releaseKeystore != null) signingConfigs.getByName("release")
                            else signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    packaging {
        resources {
            excludes += setOf("/META-INF/{AL2.0,LGPL2.1}", "/META-INF/*.kotlin_module", "DebugProbesKt.bin",
                "/META-INF/versions/**", "kotlin/**", "/*.properties")
        }
        jniLibs { useLegacyPackaging = false }
    }
    dependenciesInfo { includeInApk = false; includeInBundle = false }
    lint { abortOnError = false; checkReleaseBuilds = false }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Encrypted local storage
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("net.zetetic:sqlcipher-android:4.6.1@aar")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")

    // Security, background work, health data
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.health.connect:connect-client:1.1.0-alpha10")

    // Camera + on-device ML (barcode + image labeling)
    val camerax = "1.4.1"
    implementation("androidx.camera:camera-core:$camerax")
    implementation("androidx.camera:camera-camera2:$camerax")
    implementation("androidx.camera:camera-lifecycle:$camerax")
    implementation("androidx.camera:camera-view:$camerax")
    implementation("com.google.guava:guava:33.3.1-android")   // ListenableFuture used by CameraX
    // Unbundled ML Kit: models are delivered by Google Play services instead of shipping in the APK.
    implementation("com.google.android.gms:play-services-mlkit-barcode-scanning:18.3.1")
    implementation("com.google.android.gms:play-services-mlkit-image-labeling:16.0.8")

    // Image loading + animated GIF playback for the exercise library
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-gif:2.7.0")   // animated image (WebP) decoding

    debugImplementation("androidx.compose.ui:ui-tooling")
}
