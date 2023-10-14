buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.2")
        classpath(kotlin("gradle-plugin", version = "1.9.10"))
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.8.20")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.4")

        classpath("com.github.ben-manes:gradle-versions-plugin:0.49.0")
    }
}
