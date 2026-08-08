plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.parcelize)
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "es.pile"
    compileSdk = 37

    defaultConfig {
        applicationId = "es.pile"
        minSdk = 28
        targetSdk = 37
        versionCode = 11
        versionName = "1.1.4"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
            )
            ndk.debugSymbolLevel = "FULL"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }

    dependenciesInfo { // For kdroid key
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    // DataStore
    implementation(libs.androidx.datastore)

    //Work manager
    implementation(libs.androidx.work.runtime.ktx)

    // Navigation 3
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)


    // Compose
    implementation(libs.androidx.material3alpha)

    // Compose BOM (alinear versiones)
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Compose
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.activity)

    // Preview & Tooling
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlin.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.workmanager)
    implementation(libs.koin.compose)

    // SQL Delight Driver and Extension
    implementation(libs.sqlDelight.android.driver)
    implementation(libs.sqlDelight.coroutines.extensions)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // ExifInterface
    implementation(libs.androidx.exifinterface)

    // Reorderable List
    implementation(libs.reorderable.list)

    // Crop images - My fork
    implementation(libs.crop.kit)

    // Logging
    implementation(libs.napier)

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
}


sqldelight {
    databases {
        create("Database") {
            packageName.set("es.pile")
        }
    }
}