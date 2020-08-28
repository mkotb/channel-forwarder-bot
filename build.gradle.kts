/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 */

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.3.72"

    // Apply the application plugin to add support for building a CLI application.
    application
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()

    mavenLocal() // TODO remove

    maven {
        name = "GitHub Packages"
        url = uri("https://maven.pkg.github.com/jTelegram/jTelegramBotAPI")
        credentials {
            username = System.getProperty("gpr.user") ?: ""
            password = System.getProperty("gpr.key")
        }
    }
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    // kTelegram
    implementation("com.jtelegram:ktelegrambotapi:4.0.11")

    // Redis
    implementation("redis.clients:jedis:3.2.0")
}

application {
    // Define the main class for the application.
    mainClassName = "com.mazenk.channelforwarder.AppKt"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}

sourceSets.getByName("main") {
    java.srcDir("src/main/kotlin")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.mazenk.channelforwarder.AppKt"
    }

    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
}