//plugins {
//    id("com.android.application")
//    id("org.jetbrains.kotlin.android")
//    id("com.google.gms.google-services")
//}
//
//android {
//    namespace = "com.example.ruleoftheday333"
//    compileSdk = 34
//
//    defaultConfig {
//        applicationId = "com.example.ruleoftheday333"
//        minSdk = 24
//        targetSdk = 34
//        versionCode = 1
//        versionName = "1.0"
//    }
//
//    buildFeatures {
//        viewBinding = true
//    }
//}
//
//dependencies {
//    // Firebase
//    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
////    implementation (platform('com.google.firebase:firebase-bom:33.1.0'))
////    implementation("com.google.firebase:firebase-auth")
//    implementation("com.google.firebase:firebase-auth:22.3.1")
//    implementation("com.google.firebase:firebase-database")
//
//    // Material + UI
//    implementation("androidx.appcompat:appcompat:1.6.1")
//    implementation("com.google.android.material:material:1.12.0")
//
//    // Google Services
//    implementation("com.google.android.gms:play-services-auth:21.5.1")
//
//    // Other
//    implementation("com.squareup.okhttp3:okhttp:4.12.0")
////    implementation("com.google.guava:guava:32.1.2-android")
//    implementation("com.github.prolificinteractive:material-calendarview:2.0.1")
//
//    //SPOTIFY STUFF
//    implementation("com.squareup.retrofit2:retrofit:2.9.0")
//    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
//
//    //SONGS COVERS
////    implementation("com.github.bumptech.glide:glide:4.15.1")
//    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
//    // Glide for album cover images
//    implementation("com.github.bumptech.glide:glide:4.16.0")
////    kapt("com.github.bumptech.glide:compiler:4.16.0")
////    implementation 'com.github.bumptech.glide:glide:4.15.1'
////    annotationProcessor 'com.github.bumptech.glide:compiler:4.15.1'
//
//// ExoPlayer for 30-second previews
////    implementation("com.google.android.exoplayer:exoplayer:2.20.0")
//
//    // REQUIRED
////    implementation("androidx.media3:media3-exoplayer:1.9.2")
////    implementation("androidx.media3:media3-common:1.9.2")
//    implementation("androidx.media3:media3-exoplayer:1.9.2")
//    implementation("androidx.media3:media3-ui:1.9.2")
//
//// OPTIONAL UI component if you want playback controls (e.g., a player view)
//    implementation("androidx.media3:media3-ui:1.9.2")
//
//    //AI
//    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
//
//    implementation("com.google.guava:guava:32.1.3-android")
//}
//
////dependencies {
////    // Firebase
////    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
////    implementation("com.google.firebase:firebase-auth")
////    implementation("com.google.firebase:firebase-database")
////
////    implementation("com.google.android.material:material:1.13.0")
////
////    // Google Services
////    implementation("com.google.android.gms:play-services-auth:21.5.1")
////
////    // UI
////    implementation("androidx.appcompat:appcompat:1.6.1")
////    implementation("com.google.android.material:material:1.11.0")
////
////    // Other
////    implementation("com.squareup.okhttp3:okhttp:4.12.0")
////    implementation("com.google.guava:guava:32.1.2-android")
//////    implementation("com.prolificinteractive:material-calendarview:2.0.1")
////    implementation("com.github.prolificinteractive:material-calendarview:2.0.1")
////}
//
////apply plugin: 'com.google.gms.google-services' // at the bottom

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.ruleoftheday333"

    // MUST be 35+ because of Media3
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.ruleoftheday333"
        minSdk = 24

        // keep aligned with compileSdk (safe modern setup)
        targetSdk = 36

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
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-database")

    // Core Android UI
    implementation("androidx.appcompat:appcompat:1.6.1")

    // ✅ FIXED MATERIAL (important)
    implementation("com.google.android.material:material:1.12.0")

    // Google auth
    implementation("com.google.android.gms:play-services-auth:21.5.1")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Calendar
    implementation("com.github.prolificinteractive:material-calendarview:2.0.1")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // ✅ Media3 (clean, no duplicates)
    implementation("androidx.media3:media3-exoplayer:1.9.2")
    implementation("androidx.media3:media3-ui:1.9.2")

    // AI
//    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("org.json:json:20231013")

    // Guava (keep only one)
    implementation("com.google.guava:guava:32.1.3-android")
}