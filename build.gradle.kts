import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Copy

plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

sourceSets {
    main {
        resources {
            srcDirs("src/main/resources")
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

javafx {
    version = "21.0.8"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics")
}

application {
    mainClass = "com.ittia.gds.GDSEMR_frame"
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.45.3.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

tasks.withType<org.gradle.api.tasks.testing.Test> {
    useJUnitPlatform()
}

// Skip duplicate resources to avoid processResources failure
tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Alternatively, scope only to processResources:
// tasks.named<Copy>("processResources") {
//     duplicatesStrategy = DuplicatesStrategy.EXCLUDE
// }
