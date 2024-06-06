import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidBasePlugin
import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("com.android.application") version "8.4.1" apply false
    id("com.android.library") version "8.4.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("com.vanniktech.maven.publish") version "0.28.0" apply false
    id("com.diffplug.spotless") version "6.25.0" apply false
}

subprojects {
    plugins.withType<AndroidBasePlugin>().configureEach {
        extensions.configure<BaseExtension> {
            compileSdk = 34
            defaultConfig {
                minSdk = 21
            }
        }
    }

    plugins.withType<JavaBasePlugin>().configureEach {
        extensions.configure<JavaPluginExtension> {
             toolchain.languageVersion = JavaLanguageVersion.of(8)
        }
    }

    apply(plugin = "com.diffplug.spotless")
    extensions.configure<SpotlessExtension> {
        kotlin {
            ktlint()
            target("**/*.kt")
        }
    }
}
