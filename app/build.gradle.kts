plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.grandpyanne.daemrevise"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.grandpyanne.daemrevise"
        minSdk = 24
        targetSdk = 35
        versionCode = 17
        versionName = "17.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // The textbook PDFs are already compressed; storing them as-is makes builds faster.
    androidResources {
        noCompress += "pdf"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.15.0")
    // Home-screen widget
    implementation("androidx.glance:glance-appwidget:1.1.1")
    // Background tasks the widget uses. Newer than the version Glance brings in by default.
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
