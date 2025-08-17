plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.notsure"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.notsure"
        minSdk = 34
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
        compose = true
    }
}

dependencies {
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.persistentcookiejar)
    implementation(libs.kotlinx.serialization.json)

    implementation("com.github.franmontiel:PersistentCookieJar:v1.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation(libs.kotlinx.serialization.json)

    implementation("com.google.accompanist:accompanist-pager:0.31.3-beta")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.31.3-beta")

    implementation("androidx.compose.material3:material3:1.1.2") // or latest







    implementation (libs.retrofit)
    implementation (libs.converter.gson)

    // OkHttp (for network requests) - Retrofit uses OkHttp internally, so it's often added for advanced configuration
    implementation (libs.okhttp)
    implementation (libs.io.coil.kt.coil)
    implementation (libs.coil.kt.coil.compose)

    // OkHttp logging interceptor (optional, but helpful for debugging network requests)
    implementation (libs.logging.interceptor)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}