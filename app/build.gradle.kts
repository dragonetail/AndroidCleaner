plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.blackharry.androidcleaner"
    compileSdk = 30

    defaultConfig {
        applicationId = "com.blackharry.androidcleaner"
        minSdk = 21
        targetSdk = 30
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
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}