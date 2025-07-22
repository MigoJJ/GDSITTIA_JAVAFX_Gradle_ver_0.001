package com.ittia.gds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GDSittiaEntry extends Application {

    private static final String[] BUTTON_NAMES = {
        "Log In", "Ittia Start", "Prologue", "Version Information", "Rescue", "Quit"
    };

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ITTIA Launcher");

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");

        createButtons(root);

        Scene scene = new Scene(root, 300, 350);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createButtons(VBox root) {
        for (String name : BUTTON_NAMES) {
            Button button = new Button(name);
            button.setPrefWidth(200);
            button.setOnAction(e -> {
                try {
                    handleButtonPress(name);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Alert errorAlert = new Alert(AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("An error occurred");
                    errorAlert.setContentText("Could not open the requested file or launch application.");
                    errorAlert.showAndWait();
                } catch (Exception ex) { // Catch a general Exception for application launch issues
                    ex.printStackTrace();
                    Alert errorAlert = new Alert(AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Application Launch Error");
                    errorAlert.setContentText("Could not launch the ITTIA application.");
                    errorAlert.showAndWait();
                }
            });
            root.getChildren().add(button);
        }
    }

    private void handleButtonPress(String buttonText) throws Exception { // Changed to 'throws Exception' to cover potential start method issues
        switch (buttonText) {
            case "Prologue":
                System.out.println("Prologue selected.");
                displayResourceFile("/com/ittia/gds/txt_entry/GDSITTIA_prologue.txt", "Prologue");
                break;
            case "Version Information":
                System.out.println("Version 1.0 - July 2025.");
                displayResourceFile("/com/ittia/gds/txt_entry/GDSITTIA_IDE.txt", "Version Information");
                break;
            case "Ittia Start":
                System.out.println("Launching ITTIA...");
                launchGDSEMRFrame(); // Call the new method to launch GDSEMR_frame
                // Optionally hide the current stage
                // ((Stage) ((Button) event.getSource()).getScene().getWindow()).hide();
                break;
            case "Rescue":
                System.out.println("Rescue action triggered.");
                break;
            case "Quit":
                System.out.println("Exiting application.");
                System.exit(0);
                break;
            default:
                System.err.println("Unrecognized action for button: " + buttonText);
        }
    }

    /**
     * Reads the content of a specified resource file and displays it in an Alert dialog.
     * @param resourcePath The absolute path to the resource file within the classpath (e.g., "/path/to/file.txt").
     * @param title The title for the Alert dialog.
     * @throws IOException If the resource file cannot be found or read.
     */
    private void displayResourceFile(String resourcePath, String title) throws IOException {
        StringBuilder content = new StringBuilder();
        try (InputStream is = getClass().getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            if (is == null) {
                System.err.println("Resource not found: " + resourcePath);
                throw new IOException("File not found at: " + resourcePath);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content.toString());
            alert.getDialogPane().setPrefSize(900, 800);
            alert.showAndWait();

        } catch (NullPointerException e) {
            System.err.println("Error: Resource file not found or path is incorrect: " + resourcePath);
            throw new IOException("Resource file could not be loaded: " + resourcePath, e);
        }
    }

    /**
     * Launches the GDSEMR_frame application in a new stage.
     */
    private void launchGDSEMRFrame() throws Exception {
        // Create a new Stage for the GDSEMR_frame
        Stage gdsemrStage = new Stage();
        GDSEMR_frame.main(null); // Call the start method of GDSEMR_frame

        // Optional: Hide the current stage (GDSittiaEntry) if you want to switch applications
        // This line would need to be in the button's action handler or passed the primary stage
        // primaryStage.hide(); // You would need to make primaryStage accessible here or pass it.
                               // For simplicity, for now, they'll both be visible.
    }


    public static void main(String[] args) {
        launch(args);
    }
}