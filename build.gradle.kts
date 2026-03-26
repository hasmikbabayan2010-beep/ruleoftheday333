//// Top-level build file where you can add configuration options common to all sub-projects/modules.
//plugins {
//    id("com.google.gms.google-services") version "4.4.4" apply false
//    alias(libs.plugins.android.application) apply false
////    alias(libs.plugins.google.gms.google.services) apply false
//}
//
//plugins {
//
//    id("com.android.application") version "8.13.2" apply false
//    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
//    id("com.google.gms.google-services") version "4.4.1" apply false
//}
//
//
//buildscript {
//    dependencies {
//        classpath 'com.google.gms:google-services:4.4.1'
//    }
//}

//plugins {
//    id("com.android.application") version "8.1.3" apply false
//    id("com.google.gms.google-services") version "4.4.1" apply false
//}

//buildscript {
//    repositories {
//        google()
//        mavenCentral()
//    }
//    dependencies {
//        classpath 'com.android.tools.build:gradle:8.1.2'
//        classpath 'com.google.gms:google-services:4.4.1'
//    }
//}
//
//allprojects {
//    repositories {
//        google()
//        mavenCentral()
//    }
//}

// build.gradle.kts (project-level)
//plugins {
//    // No need to put google-services here
//    // Only use this for gradle plugin versions if desired
//}

buildscript {
//    repositories {
//        google()
//        mavenCentral()
//    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.13.2")
        classpath("com.google.gms:google-services:4.4.1")
    }
}
plugins {
    // You can use the plugins DSL instead of buildscript if using modern Gradle
    id("com.android.application") version "8.1.2" apply false
    id("com.android.library") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
}

//allprojects {
//    repositories {
//        google()
//        mavenCentral()
//    }
//}