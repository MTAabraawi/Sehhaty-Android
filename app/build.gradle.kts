plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
}

// Safely apply google-services only if google-services.json exists
// This guarantees successful builds even before Firebase credentials are added by user
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.mtaapps.sehhaty"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mtaapps.sehhaty"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // AdMob Official Test App ID
        manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
    }

    signingConfigs {
        create("release") {
            // Configured via environment variables / Gradle properties for GitHub Actions
            val keystorePath = System.getenv("KEYSTORE_PATH") ?: project.findProperty("KEYSTORE_PATH") as? String
            if (keystorePath != null && file(keystorePath).exists()) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: project.findProperty("KEYSTORE_PASSWORD") as? String
                keyAlias = System.getenv("KEY_ALIAS") ?: project.findProperty("KEY_ALIAS") as? String
                keyPassword = System.getenv("KEY_PASSWORD") ?: project.findProperty("KEY_PASSWORD") as? String
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use release signing config if available, fallback to debug for automated testing
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null && releaseSigning.storeFile!!.exists()) {
                signingConfig = releaseSigning
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Room Database (Local Persistence)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    // Using runtime sqlite queries & room-compiler if needed

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Firebase (BoM guarantees version compatibility)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // Google Play Services & AdMob
    implementation(libs.play.services.auth)
    implementation(libs.play.services.ads)

    // Unit Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
