plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    application
}

group = "org.nedz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-ide-plugin-dependencies") {
        content {
            includeGroup("org.jetbrains.kotlin")
        }
    }
}

dependencies {
    listOf(
        // Source of these artifacts is
        // https://github.com/JetBrains/kotlin/tree/v2.0.21/prepare/ide-plugin-dependencies
        // where ones whose name contains "high-level" are deprecated and should not be used - see
        // https://github.com/JetBrains/kotlin/commit/3ad9798a17ad9eb68cdb1e9f8f1a69584151bfd4
        "org.jetbrains.kotlin:analysis-api-standalone-for-ide",
        "org.jetbrains.kotlin:analysis-api-platform-interface-for-ide",
        "org.jetbrains.kotlin:analysis-api-for-ide", // old name "high-level-api-for-ide"
        "org.jetbrains.kotlin:analysis-api-impl-base-for-ide", // old name "high-level-api-impl-base"
        "org.jetbrains.kotlin:analysis-api-k2-for-ide", // old name "high-level-api-k2"
        "org.jetbrains.kotlin:low-level-api-fir-for-ide",
        "org.jetbrains.kotlin:symbol-light-classes-for-ide"
    ).forEach {
        api("$it:2.0.20") {
            // https://youtrack.jetbrains.com/issue/KT-61639/Standalone-Analysis-API-cannot-find-transitive-dependencies
            isTransitive = false
        }
    }
    implementation("com.github.ben-manes.caffeine:caffeine:2.9.3")

    implementation( "org.jetbrains.kotlin", "kotlin-compiler", "2.0.20")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // Ktor server
    implementation("io.ktor:ktor-server-core:2.3.3")
    implementation("io.ktor:ktor-server-netty:2.3.3")
    implementation("io.ktor:ktor-server-html-builder:2.3.3")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.3")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.8")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}
