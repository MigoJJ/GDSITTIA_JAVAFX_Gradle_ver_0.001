package com.ittia.gds;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class GDSEMR_frame extends Application {
    private static final double FRAME_WIDTH = 1350;
    private static final double FRAME_HEIGHT = 900;
    public static final String[] TEXT_AREA_TITLES = {
        "CC>", "PI>", "ROS>", "PMH>", "S>",
        "O>", "Physical Exam>", "A>", "P>", "Comment>"
    };
    public static TextArea[] textAreas;
    public static TextArea tempOutputArea;
    public static TextField gradientInputField;

    @Override
    public void start(Stage primaryStage) {
        textAreas = new TextArea[TEXT_AREA_TITLES.length];
        BooleanProperty cleared = new SimpleBooleanProperty(false);

        for (int i = 0; i < TEXT_AREA_TITLES.length; i++) {
            TextArea ta = new TextArea();
            ta.setPromptText(TEXT_AREA_TITLES[i]);
            ta.setFont(Font.font("Consolas", 13));
            ta.setWrapText(true);
            ta.setPrefRowCount(3);
            ta.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-border-radius: 5px;");

            ta.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal && !cleared.get()) {
                    for (TextArea t : textAreas) {
                        if (t != null) t.clear();
                    }
                    cleared.set(true);
                }
            });

            // Abbreviation handling example (customize as needed)
            ta.setOnKeyReleased(evt -> {
                if (evt.getCode() == KeyCode.SPACE) {
                    // Your abbreviation handling logic here
                }
            });

            // Function key and double-click handlers (placeholders)
            ta.setOnKeyPressed(evt -> { /* ... */ });
            ta.setOnMouseClicked(evt -> { /* ... */ });

            textAreas[i] = ta; // Append to the array for later access
        }

        // Output area on the right, styled
        tempOutputArea = new TextArea();
        tempOutputArea.setEditable(true);
        tempOutputArea.setPromptText("Output");
        tempOutputArea.setFont(Font.font("Consolas", 13));
        tempOutputArea.setWrapText(true);
        tempOutputArea.setStyle("-fx-border-color: #d0d0d0; -fx-border-width: 1px; -fx-border-radius: 5px;");

        gradientInputField = new TextField();
        gradientInputField.setPromptText("Input command here...");
        gradientInputField.setFont(Font.font("Consolas", 13));
        gradientInputField.setStyle("-fx-border-color: #d0d0d0; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-color: #f8f8f8;");

        // Listening: Option 1 - Mirror all textareas' content into output on change
        for (int i = 0; i < TEXT_AREA_TITLES.length; i++) {
            textAreas[i].textProperty().addListener((obs, oldVal, newVal) -> {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < TEXT_AREA_TITLES.length; j++) {
                    String content = textAreas[j].getText();
                    if (content != null && !content.trim().isEmpty()) {
                        sb.append(TEXT_AREA_TITLES[j]).append(" ").append(content.trim()).append("\n\n");
                    }
                }
                tempOutputArea.setText(sb.toString().trim());
            });
        }

        // Left Grid: label+area groups
        GridPane leftGrid = new GridPane();
        leftGrid.setHgap(15);
        leftGrid.setVgap(10);
        leftGrid.setPadding(new Insets(15));
        leftGrid.setStyle("-fx-background-color: #f5f5f5; -fx-border-radius: 10px; -fx-background-radius: 10px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        for (int i = 0; i < TEXT_AREA_TITLES.length; i++) {
            Label lbl = new Label(TEXT_AREA_TITLES[i]);
            lbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            lbl.setTextFill(Color.web("#333333"));
            VBox section = new VBox(3, lbl, textAreas[i]);
            VBox.setVgrow(textAreas[i], Priority.ALWAYS);
            GridPane.setConstraints(section, i % 2, i / 2);
            leftGrid.getChildren().add(section);
        }
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        leftGrid.getColumnConstraints().addAll(col1, col2);

        // Right: output+input w/ gradient
        VBox rightBox = new VBox(15);
        rightBox.setPadding(new Insets(15));
        rightBox.setPrefWidth(450);
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(230, 245, 230)),
            new Stop(1, Color.rgb(200, 230, 200))
        );
        rightBox.setBackground(
            new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundFill(gradient, new javafx.scene.layout.CornerRadii(10), Insets.EMPTY)
            )
        );
        rightBox.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        ScrollPane outSp = new ScrollPane(tempOutputArea);
        outSp.setFitToWidth(true);
        outSp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        VBox.setVgrow(outSp, Priority.ALWAYS);
        rightBox.getChildren().addAll(outSp, gradientInputField);

        // Split pane layout
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(leftGrid, rightBox);
        splitPane.setDividerPositions(0.68);

        // Button panels
        HBox northPanel = new HBox(15);
        northPanel.setPadding(new Insets(10, 15, 10, 15));
        northPanel.setAlignment(Pos.CENTER_RIGHT);
        northPanel.setStyle("-fx-background-color: #e8e8e8;");

        HBox southPanel = new HBox(15);
        southPanel.setPadding(new Insets(10, 15, 10, 15));
        southPanel.setAlignment(Pos.CENTER_LEFT);
        southPanel.setStyle("-fx-background-color: #e8e8e8;");

        String[] btns = {"Save", "Load", "Clear", "Submit"};
        for (String name : btns) {
            Button b1 = new Button(name);
            b1.setPrefWidth(80);
            b1.setStyle("-fx-background-color: #5cb85c; -fx-text-fill: white; -fx-font-weight: bold; " +
                       "-fx-border-radius: 5px; -fx-background-radius: 5px;");
            b1.setOnMouseEntered(e -> b1.setStyle("-fx-background-color: #4cae4c; -fx-text-fill: white; -fx-font-weight: bold; " +
                       "-fx-border-radius: 5px; -fx-background-radius: 5px;"));
            b1.setOnMouseExited(e -> b1.setStyle("-fx-background-color: #5cb85c; -fx-text-fill: white; -fx-font-weight: bold; " +
                       "-fx-border-radius: 5px; -fx-background-radius: 5px;"));
            // placeholder: attach your handler here, e.g., MainFrame_Button_north_south.EMR_B_1entryentry(name, "north"));
            northPanel.getChildren().add(b1);

            Button b2 = new Button(name);
            b2.setPrefWidth(80);
            b2.setStyle("-fx-background-color: #0275d8; -fx-text-fill: white; -fx-font-weight: bold; " +
                       "-fx-border-radius: 5px; -fx-background-radius: 5px;");
            b2.setOnMouseEntered(e -> b2.setStyle("-fx-background-color: #025aa5; -fx-text-fill: white; -fx-font-weight: bold; " +
                       "-fx-border-radius: 5px; -fx-background-radius: 5px;"));
            b2.setOnMouseExited(e -> b2.setStyle("-fx-background-color: #0275d8; -fx-text-fill: white; -fx-font-weight: bold; " +
                       "-fx-border-radius: 5px; -fx-background-radius: 5px;"));
            // placeholder: attach your handler here, e.g., MainFrame_Button_north_south.EMR_B_1entryentry(name, "south"));
            southPanel.getChildren().add(b2);
        }

        // Root pane
        BorderPane root = new BorderPane();
        root.setCenter(splitPane);
        root.setTop(northPanel);
        root.setBottom(southPanel);
        root.setStyle("-fx-background-color: #ffffff;");

        Scene scene = new Scene(root, FRAME_WIDTH, FRAME_HEIGHT);
        primaryStage.setTitle("GDS EMR Interface for Physician - Enhanced");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
