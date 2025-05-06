plugins {
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("org.jetbrains.compose") version "1.7.3"
    id("application")
    kotlin("plugin.serialization") version "2.0.21"
}

group = "com.dat"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainClass = "com.dat.activity_tracker.MainKt"
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // Jetpack Compose for Desktop
    implementation(compose.desktop.currentOs)
    implementation(compose.material)
    implementation(compose.materialIconsExtended)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")

    // SQLite (we'll use JDBC directly for desktop instead of Room)
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")

    // JSON
    implementation("org.json:json:20240303")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Charts for reports
    implementation("org.jfree:jfreechart:1.5.4")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // Implementation for Windows-specific monitoring
    implementation("net.java.dev.jna:jna:5.13.0")
    implementation("net.java.dev.jna:jna-platform:5.13.0")

    // Python Integration (Jython)
    implementation("org.python:jython-standalone:2.7.3")
    // Add SLF4J implementation
    implementation("org.slf4j:slf4j-simple:1.7.36")

    implementation("com.mohamedrejeb.dnd:compose-dnd:0.3.0")
}
