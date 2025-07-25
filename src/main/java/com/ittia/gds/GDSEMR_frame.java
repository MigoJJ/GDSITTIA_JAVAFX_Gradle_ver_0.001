package com.ittia.gds;

import com.ittia.gds.db.DatabaseManager;
import com.ittia.gds.ui.mainframe.buttons.GDSFrameButtonExe; // Import the new class
import com.ittia.gds.ui.mainframe.changestring.AbbreviationManagerUI;
import com.ittia.gds.ui.mainframe.changestring.AbbreviationsMain;

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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
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
            textAreas[i] = ta;
        }

        tempOutputArea = new TextArea();
        tempOutputArea.setEditable(true);
        tempOutputArea.setPromptText("Output");
        tempOutputArea.setFont(Font.font("Consolas", 13));
        tempOutputArea.setWrapText(true);
        tempOutputArea.setPrefRowCount(40);
        tempOutputArea.setStyle("-fx-border-color: #d0d0d0; -fx-border-width: 1px; -fx-border-radius: 5px;");

        DatabaseManager dbManager = new DatabaseManager();
        AbbreviationsMain abbreviationHandler = new AbbreviationsMain(textAreas, tempOutputArea, TEXT_AREA_TITLES);
        AbbreviationManagerUI abbreviationManagerUI = new AbbreviationManagerUI(abbreviationHandler, dbManager);

        for (TextArea ta : textAreas) {
            ta.textProperty().addListener((obs, oldVal, newVal) -> {
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

        GridPane rightInputGrid = new GridPane();
        rightInputGrid.setHgap(15);
        rightInputGrid.setVgap(10);
        rightInputGrid.setPadding(new Insets(15));
        rightInputGrid.setStyle("-fx-background-color: #f5f5f5; -fx-border-radius: 10px; -fx-background-radius: 10px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        for (int i = 0; i < TEXT_AREA_TITLES.length; i++) {
            Label lbl = new Label(TEXT_AREA_TITLES[i]);
            lbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            lbl.setTextFill(Color.web("#333333"));
            VBox section = new VBox(3, lbl, textAreas[i]);
            VBox.setVgrow(textAreas[i], Priority.ALWAYS);
            GridPane.setConstraints(section, i % 2, i / 2);
            rightInputGrid.getChildren().add(section);
        }

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        rightInputGrid.getColumnConstraints().addAll(col1, col2);

        for (int i = 0; i < 5; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            rc.setPercentHeight(20);
            rightInputGrid.getRowConstraints().add(rc);
        }

        VBox leftOutputPane = new VBox();
        leftOutputPane.setPadding(new Insets(15));
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(230, 245, 230)),
                new Stop(1, Color.rgb(200, 230, 200))
        );
        leftOutputPane.setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(gradient, new javafx.scene.layout.CornerRadii(10), Insets.EMPTY)));
        leftOutputPane.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        ScrollPane outSp = new ScrollPane(tempOutputArea);
        outSp.setFitToWidth(true);
        outSp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        VBox.setVgrow(outSp, Priority.ALWAYS);
        leftOutputPane.getChildren().add(outSp);

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(leftOutputPane, rightInputGrid);
        splitPane.setDividerPositions(0.5);

        // ======================= BUTTON SECTION START =======================

        // --- Create an instance of the button executor ---
        GDSFrameButtonExe buttonExecutor = new GDSFrameButtonExe(textAreas, tempOutputArea);

        // --- North Panel ---
        HBox northPanel = new HBox(15);
        northPanel.setPadding(new Insets(10, 15, 10, 15));
        northPanel.setAlignment(Pos.CENTER_RIGHT);
        northPanel.setStyle("-fx-background-color: #e8e8e8;");

        Button manageAbbrBtn = new Button("Manage Abbreviations");
        manageAbbrBtn.setStyle("-fx-background-color: #f0ad4e; -fx-text-fill: white; -fx-font-weight: bold;");
        manageAbbrBtn.setOnAction(e -> abbreviationManagerUI.display());
        northPanel.getChildren().add(manageAbbrBtn);

        // --- NEW: Add ICD11 Button ---
        Button icdButton = new Button("ICD11");
        icdButton.setStyle("-fx-background-color: #5bc0de; -fx-text-fill: white; -fx-font-weight: bold;");
        icdButton.setOnAction(e -> {
            System.out.println("ICD11 button clicked.");
            // TODO: Implement ICD11 functionality
        });
        northPanel.getChildren().add(icdButton);

        // --- NEW: Add Laboratory Button ---
        Button labButton = new Button("Laboratory");
        labButton.setStyle("-fx-background-color: #5bc0de; -fx-text-fill: white; -fx-font-weight: bold;");
        labButton.setOnAction(e -> {
            System.out.println("Laboratory button clicked.");
            // TODO: Implement Laboratory functionality
        });
        northPanel.getChildren().add(labButton);


        String[] northButtonTitles = {"SaveRescue", "Load", "Clear","Exit"};
        for (String title : northButtonTitles) {
            Button btn = createStyledButton(title, "#5cb85c", "#4cae4c");
            buttonExecutor.attach(btn, title); // Use the executor to attach actions
            northPanel.getChildren().add(btn);
        }

        // --- South Panel ---
        HBox southPanel = new HBox(15);
        southPanel.setPadding(new Insets(10, 15, 10, 15));
        southPanel.setAlignment(Pos.CENTER_LEFT);
        southPanel.setStyle("-fx-background-color: #e8e8e8;");

        String[] southButtonTitles = {"Save", "Load", "Clear", "CE", "Submit"};
        for (String title : southButtonTitles) {
            Button btn = createStyledButton(title, "#0275d8", "#025aa5");
            buttonExecutor.attach(btn, title); // Use the executor to attach actions
            southPanel.getChildren().add(btn);
        }

        // ======================== BUTTON SECTION END ========================

        BorderPane root = new BorderPane();
        root.setCenter(splitPane);
        root.setTop(northPanel);
        root.setBottom(southPanel);
        root.setStyle("-fx-background-color: #ffffff;");
        root.setPrefSize(FRAME_WIDTH, FRAME_HEIGHT);

        Scene scene = new Scene(root, FRAME_WIDTH, FRAME_HEIGHT);
        primaryStage.setTitle("GDS EMR Interface for Physician - Enhanced");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();
    }

    private Button createStyledButton(String text, String baseColor, String hoverColor) {
        Button button = new Button(text);
        button.setPrefWidth(80);
        final String baseStyle = String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5px; -fx-background-radius: 5px;", baseColor);
        final String hoverStyle = String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5px; -fx-background-radius: 5px;", hoverColor);
        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        return button;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
