plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.nhc.wear"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nhc.qrscananddisplay"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

//    productFlavors {
//        create("free") {
//            applicationIdSuffix = ".free"
//            versionNameSuffix = "-free"
//            dimension = "version"
//            buildConfigField("String", "TARGET_PHONE_APP_ID", "\"com.nhc.qrscananddisplay.free\"")
//        }
//        create("pro") {
//            applicationIdSuffix = ".pro"
//            versionNameSuffix = "-pro"
//            dimension = "version"
//            buildConfigField("String", "TARGET_PHONE_APP_ID", "\"com.nhc.qrscananddisplay.pro\"")
//        }
//    }

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

    implementation(libs.play.services.wearable)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}