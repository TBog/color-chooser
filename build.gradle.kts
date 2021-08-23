buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.1")
        classpath(kotlin("gradle-plugin", version = "1.5.21"))
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.5.0")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.5")

        classpath("com.github.ben-manes:gradle-versions-plugin:0.39.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.create("clean", Delete::class) {
    delete(rootProject.buildDir)
}
