plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.prog_poe_final"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.prog_poe_final"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //noinspection GradleDependency
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.activity.ktx)

    // Networking (Retrofit + GSON)
    implementation (libs.retrofit)
    implementation (libs.retrofit2.converter.gson)

    // Logging
    implementation (libs.logging.interceptor)

    // Kotlin coroutines (for background calls)
    implementation (libs.kotlinx.coroutines.core)
    implementation (libs.jetbrains.kotlinx.coroutines.android)

    // Biometric
    implementation (libs.androidx.biometric)
    implementation (libs.androidx.security.crypto)

    // Firebase
    //noinspection BomWithoutPlatform
    implementation(libs.firebase.bom)
    implementation (libs.firebase.messaging)
    implementation (libs.firebase.analytics)

    




}