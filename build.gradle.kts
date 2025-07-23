plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.14"
}

application {
    mainModule.set("com.ittia.gds")
    mainClass.set("com.ittia.gds.GDSittiaEntry")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.openjfx:javafx-controls:21.0.1")
    implementation("org.openjfx:javafx-fxml:21.0.1")
    implementation("org.openjfx:javafx-graphics:21.0.1") // 그래픽 모듈 추가
    implementation("org.xerial:sqlite-jdbc:3.45.1.0") // Check for the latest version
}

javafx {
    version = "21.0.1"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics")
}