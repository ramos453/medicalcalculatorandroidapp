// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false // Add this line
    alias(libs.plugins.navigation.safeargs) apply false
    alias(libs.plugins.navigation.safeargs.kotlin) apply false

    // 🔥 UPDATED FOR FIREBASE MIGRATION
    id("com.google.gms.google-services") version "4.4.2" apply false // Updated version
    id("com.google.firebase.crashlytics") version "3.0.2" apply false // Added for error tracking
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
//plugins {
//    alias(libs.plugins.android.application) apply false
//    alias(libs.plugins.kotlin.android) apply false
//    alias(libs.plugins.kotlin.compose) apply false
//    alias(libs.plugins.ksp) apply false // Add this line
//    alias(libs.plugins.navigation.safeargs) apply false
//    alias(libs.plugins.navigation.safeargs.kotlin) apply false
//    id("com.google.gms.google-services") version "4.4.0" apply false
//}