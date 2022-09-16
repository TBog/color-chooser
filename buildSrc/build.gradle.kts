plugins {
    kotlin("jvm") version "1.7.10"
    `kotlin-dsl`
}

repositories {
    google()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.android.tools.build:gradle:7.3.0")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.42.0")
    implementation("org.jetbrains.dokka:dokka-core:1.7.10")
}
