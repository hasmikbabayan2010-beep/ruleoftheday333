plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

    // Material + UI
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // Google Services
    implementation("com.google.android.gms:play-services-auth:21.5.1")

    // Other
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.guava:guava:32.1.2-android")
    implementation("com.github.prolificinteractive:material-calendarview:2.0.1")
}

//dependencies {
//    // Firebase
//    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
//    implementation("com.google.firebase:firebase-auth")
//    implementation("com.google.firebase:firebase-database")
//
//    implementation("com.google.android.material:material:1.13.0")
//
//    // Google Services
//    implementation("com.google.android.gms:play-services-auth:21.5.1")
//
//    // UI
//    implementation("androidx.appcompat:appcompat:1.6.1")
//    implementation("com.google.android.material:material:1.11.0")
//
//    // Other
//    implementation("com.squareup.okhttp3:okhttp:4.12.0")
//    implementation("com.google.guava:guava:32.1.2-android")
////    implementation("com.prolificinteractive:material-calendarview:2.0.1")
//    implementation("com.github.prolificinteractive:material-calendarview:2.0.1")
//}

//apply plugin: 'com.google.gms.google-services' // at the bottom