// Apply the Java plugin to add support for Java.
plugins {
    id("java") // Correct Kotlin DSL syntax for applying core 'java' plugin
    // Apply the application plugin to add support for building a runnable JAR.
    id("application") // Correct Kotlin DSL syntax for applying core 'application' plugin
    // Apply the JavaFX plugin for JavaFX specific tasks and configurations.
    // This plugin simplifies adding JavaFX dependencies and configuring the run task.
    id("org.openjfx.javafxplugin") version "0.1.0" // Kotlin DSL syntax for applying plugins by ID and version
}

// Configure the Java toolchain to ensure consistency with your installed JDK.
// This is important for Gradle to pick up the correct Java 21 version.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21) // Matches your OpenJDK 21.0.7
    }
}

// Specify where to find project dependencies
repositories {
    mavenCentral()
}

// JavaFX specific configuration
javafx {
    version = "21.0.8" // This matches your JavaFX SDK 21.0.8
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics")
    // Added 'javafx.graphics' as it's often implicitly needed by 'javafx.controls' and 'javafx.fxml'.
    // If you use other JavaFX features (e.g., javafx.web, javafx.media, javafx.swing),
    // remember to add them to this list.
}

// Application settings for running the app
application {
    // The main class of your application
    mainClass = "com.ittia.gds.GDSEMR_frame" // Kotlin DSL: direct assignment instead of .set()

    // If you are using Java Modules (module-info.java), keep this line.
    // If not, you can remove it. If you encounter "Error: Module com.ittia.gds not found",
    // it likely means you don't have a module-info.java, and you should remove this line.
    // Commenting out mainModule for now, as it's a common source of launch issues if module-info.java is missing or incorrect.
    // If your project *does* use a module-info.java, uncomment this and ensure the module name matches.
    // mainModule = "com.ittia.gds"
}

// This block is for any OTHER libraries you might need
dependencies {
    // Example: implementation("com.google.code.gson:gson:2.10.1")
    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0") // Kotlin DSL: use parentheses and double quotes
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0") // Kotlin DSL: use parentheses and double quotes
}

// Configure the 'test' task to use JUnit Platform.
// In Kotlin DSL, when configuring a task of a specific type (like Test),
// you can use 'withType<Test>' or 'named<Test>' to get a type-safe accessor.
tasks.withType<org.gradle.api.tasks.testing.Test> { // Explicitly configure tasks of type Test
    useJUnitPlatform()
}

// The 'run' task is automatically configured by the 'org.openjfx.javafxplugin'
// to correctly include JavaFX modules on the module path.
// You typically do not need to add explicit jvmArgs for module path when using this plugin,
// unless you have very specific runtime requirements.
