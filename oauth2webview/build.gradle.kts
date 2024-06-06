plugins {
    id("com.android.library")
    id ("org.jetbrains.kotlin.android")
    id ("com.vanniktech.maven.publish")
}

android {
    namespace = "io.goooler.oauth2webview"
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")
}
