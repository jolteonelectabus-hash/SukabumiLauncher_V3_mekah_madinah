plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.sukabumi.tvlauncher"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sukabumi.tvlauncher"
        minSdk = 23
        targetSdk = 32
        versionCode = 3
        versionName = "3.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
