plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
}

allprojects {
    group = findProperty("GROUP") as String? ?: "com.github.tlozovyi.week-view-compose"
    version = findProperty("VERSION_NAME") as String? ?: "0.1.0-alpha"
}
