import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    namespace = "com.stepbystepdrawing.HowToDrawPoppyPlaytime"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.skibdrawt.official"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Launcher label, splash title, and anywhere that uses @string/app_name
        resValue("string", "app_name", "Skibidi Toilet Draw Skib")

        buildConfigField("String", "AD_CONFIG_URL", "\"${localProperties.getProperty("AD_CONFIG_URL", "")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val ksFile = rootProject.file("keystore.jks")
            if (ksFile.exists()) {
                storeFile = ksFile
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: localProperties.getProperty("KEYSTORE_PASSWORD", "")
                keyAlias = System.getenv("KEY_ALIAS") ?: localProperties.getProperty("KEY_ALIAS", "")
                keyPassword = System.getenv("KEY_PASSWORD") ?: localProperties.getProperty("KEY_PASSWORD", "")
            }
        }
    }

    buildTypes {
        release {
            // Enable code optimization (R8/ProGuard)
            isMinifyEnabled = true

            // Enable resource shrinking
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Enable native debug symbols
            ndk {
                debugSymbolLevel = "FULL"
            }

            val ksFile = rootProject.file("keystore.jks")
            if (ksFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
}

dependencies {
    // Monetization SDKs (AdMob, AppLovin MAX, Unity Ads, Unity LevelPlay / ironSource mediation, Meta Audience Network)
    implementation(libs.play.services.ads)
    implementation(libs.applovin.sdk)
    implementation(libs.unity.ads)
    implementation(libs.unity.mediation.sdk)
    implementation(libs.facebook.audience.network)

    // Remote config fetch
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Analytics & push
    implementation("com.mixpanel.android:mixpanel-android:7.+")
    implementation("com.onesignal:OneSignal:[5.6.1, 5.6.99]")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}