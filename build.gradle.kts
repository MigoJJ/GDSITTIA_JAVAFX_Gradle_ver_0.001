plugins {
    // Enables building a Java application
    application
    
    // The key plugin for managing JavaFX dependencies and run configurations
    id("org.openjfx.javafxplugin") version "0.1.0"
}

// Specify where to find project dependencies
repositories {
    mavenCentral()
}

// JavaFX specific configuration
javafx {
    version = "21.0.8" // This matches your SDK and Java version
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics")
}

// Application settings for running the app
application {
    // The main class of your application
    mainClass.set("com.ittia.gds.GDSEMR_frame")
    
    // If you are using Java Modules (module-info.java), keep this line.
    // If not, you can remove it.
    mainModule.set("com.ittia.gds") 
}

// This block is for any OTHER libraries you might need
dependencies {
    // Example: implementation("com.google.code.gson:gson:2.10.1")
}