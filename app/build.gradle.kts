plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

android {
    namespace = "com.mroldl001.mimochat"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.mroldl001.mimochat"
        minSdk = 33
        targetSdk = 37
        versionCode = 21
        versionName = "1.3.13"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation(platform("androidx.compose:compose-bom:2026.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("top.yukonga.miuix.kmp:miuix-ui-android:0.9.1")
    implementation("top.yukonga.miuix.kmp:miuix-preference-android:0.9.1")
    implementation("top.yukonga.miuix.kmp:miuix-icons-android:0.9.1")
    implementation("top.yukonga.miuix.kmp:miuix-blur-android:0.9.1")
    
    implementation("com.google.dagger:hilt-android:2.57.2")
    kapt("com.google.dagger:hilt-compiler:2.57.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    kapt("androidx.room:room-compiler:2.8.4")
    
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.github.jeziellago:compose-markdown:0.7.2")
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.05.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
