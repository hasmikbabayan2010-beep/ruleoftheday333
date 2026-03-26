////////val org.gradle.accessors.dm.LibrariesForLibs.FirebaseLibraryAccessors.bom: kotlin.Any
//////
//////plugins {
//////    id("com.android.application")
//////    id("org.jetbrains.kotlin.android")
////////    alias(libs.plugins.android.application)
////////    alias(libs.plugins.google.gms.google.services)
//////    id("com.google.gms.google-services")
//////}
//////
//////
//////android {
//////    namespace = "com.example.ruleoftheday333"
//////    compileSdk = 36
//////
//////    defaultConfig {
//////        applicationId = "com.example.ruleoftheday333"
//////        minSdk = 24
//////        targetSdk = 36
//////        versionCode = 1
//////        versionName = "1.0"
//////
//////        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//////    }
//////
//////    buildTypes {
//////        release {
//////            isMinifyEnabled = false
//////            proguardFiles(
//////                getDefaultProguardFile("proguard-android-optimize.txt"),
//////                "proguard-rules.pro"
//////            )
//////        }
//////    }
//////    compileOptions {
//////        sourceCompatibility = JavaVersion.VERSION_11
//////        targetCompatibility = JavaVersion.VERSION_11
//////    }
//////    buildFeatures {
//////        viewBinding = true
//////    }
//////}
//////
//////dependencies {
////////    implementation("com.google.firebase:firebase-auth:22.3.1")
////////    implementation("com.google.android.gms:play-services-auth:20.7.0")
//////    implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
////////    implementation("com.google.firebase:firebase-database:20.3.0")
//////    implementation platform('com.google.firebase:firebase-bom:32.7.0')
//////    implementation 'com.google.firebase:firebase-auth'
//////    implementation 'com.google.firebase:firebase-database'
////////    implementation(libs.firebase.bom)
//////    implementation(libs.appcompat)
//////    implementation(libs.material)
//////    implementation(libs.activity)
//////    implementation("com.google.android.material:material:1.11.0")
//////    implementation(libs.constraintlayout)
//////    implementation(libs.firebase.analytics)
//////    implementation(libs.annotation)
//////    implementation(libs.lifecycle.livedata.ktx)
//////    implementation(libs.lifecycle.viewmodel.ktx)
//////    implementation(libs.firebase.auth)
//////    implementation("com.google.android.gms:play-services-auth:20.7.0")
//////    testImplementation(libs.junit)
//////    androidTestImplementation(libs.ext.junit)
//////    androidTestImplementation(libs.espresso.core)
//////}
////
////plugins {
////    id 'com.android.application'
////    id 'com.google.gms.google-services'
////}
////
////android {
////    namespace 'com.example.ruleoftheday333'
////    compileSdk 34
////
////    defaultConfig {
////        applicationId "com.example.ruleoftheday333"
////        minSdk 24
////        targetSdk 34
////        versionCode 1
////        versionName "1.0"
////    }
////}
////
////dependencies {
////
////    implementation platform('com.google.firebase:firebase-bom:32.7.0')
////
////    implementation 'com.google.firebase:firebase-auth'
////    implementation 'com.google.firebase:firebase-database'
////
////    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
////}
//
//plugins {
//    id 'com.android.application'
//    id 'com.google.gms.google-services'
//}
//
//android {
//    namespace 'com.example.ruleoftheday333'
//    compileSdk 34
//
//    defaultConfig {
//        applicationId "com.example.ruleoftheday333"
//        minSdk 24
//        targetSdk 34
//        versionCode 1
//        versionName "1.0"
//    }
//
//    buildFeatures {
//        viewBinding true
//    }
//}
//
//dependencies {
//
//    implementation platform('com.google.firebase:firebase-bom:32.7.0')
//
//    implementation 'com.google.firebase:firebase-auth'
//    implementation 'com.google.firebase:firebase-database'
//
//    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
//
//    implementation 'androidx.appcompat:appcompat:1.6.1'
//    implementation 'com.google.android.material:material:1.11.0'
//}

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.ruleoftheday333"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ruleoftheday333"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.google.guava:guava:32.1.2-android")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
}

