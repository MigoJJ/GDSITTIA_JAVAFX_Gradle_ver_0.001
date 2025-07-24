package com.ittia.gds.ui.mainframe.changestring;

import java.util.Map;
import java.util.Optional;

import com.ittia.gds.db.DatabaseManager;
import com.ittia.gds.ui.model.Abbreviation;
import com.ittia.gds.ui.model.GDSEMR_Abbreviations;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AbbreviationManagerUI {

    private final DatabaseManager dbManager;
    private final GDSEMR_Abbreviations abbreviationHandler;
    private final TableView<Abbreviation> tableView = new TableView<>();
    private final ObservableList<Abbreviation> abbreviationList = FXCollections.observableArrayList();

    public AbbreviationManagerUI2(GDSEMR_Abbreviations handler, DatabaseManager dbManager) {
        this.abbreviationHandler = handler;
        this.dbManager = dbManager;
    }

    public void display() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Abbreviation Manager");

        // --- Table View Setup ---
        TableColumn<Abbreviation, String> keyCol = new TableColumn<>("Key");
        keyCol.setCellValueFactory(new PropertyValueFactory<>("key"));
        keyCol.setPrefWidth(150);

        TableColumn<Abbreviation, String> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueCol.setPrefWidth(350);

        tableView.getColumns().addAll(keyCol, valueCol);
        FilteredList<Abbreviation> filteredData = new FilteredList<>(abbreviationList, p -> true);
        tableView.setItems(filteredData);
        loadAbbreviations(); // Load data from DB into the list

        // --- Controls Setup ---
        TextField findField = new TextField();
        findField.setPromptText("Find by key or value...");
        findField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(abbreviation -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lowerCaseFilter = newVal.toLowerCase();
                return abbreviation.getKey().toLowerCase().contains(lowerCaseFilter)
                    || abbreviation.getValue().toLowerCase().contains(lowerCaseFilter);
            });
        });

        Button addButton = new Button("Add...");
        addButton.setOnAction(e -> showAddDialog());

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteSelected());

        Button quitButton = new Button("Quit");
        quitButton.setOnAction(e -> stage.close());

        HBox controls = new HBox(10, findField, addButton, deleteButton, quitButton);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER_LEFT);

        // --- Layout ---
        BorderPane layout = new BorderPane();
        layout.setCenter(tableView);
        layout.setBottom(controls);

        Scene scene = new Scene(layout, 600, 400);
        stage.setScene(scene);
        stage.showAndWait(); // Use showAndWait to block interaction with the main window
    }

    private void loadAbbreviations() {
        abbreviationList.clear();
        Map<String, String> allAbbrs = dbManager.getAllAbbreviations();
        allAbbrs.forEach((key, value) -> abbreviationList.add(new Abbreviation(key, value)));
        abbreviationHandler.refreshAbbreviations(); // Notify the main handler to refresh its map
    }
    
    private void showAddDialog() {
        Dialog<Abbreviation> dialog = new Dialog<>();
        dialog.setTitle("Add/Edit Abbreviation");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField keyField = new TextField();
        keyField.setPromptText("Key (e.g., htn)");
        TextField valueField = new TextField();
        valueField.setPromptText("Value (e.g., Hypertension)");

        grid.add(new Label("Key:"), 0, 0);
        grid.add(keyField, 1, 0);
        grid.add(new Label("Value:"), 0, 1);
        grid.add(valueField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (!keyField.getText().isBlank() && !valueField.getText().isBlank()) {
                    return new Abbreviation(keyField.getText(), valueField.getText());
                }
            }
            return null;
        });

        Optional<Abbreviation> result = dialog.showAndWait();
        result.ifPresent(abbr -> {
            dbManager.addOrUpdateAbbreviation(abbr.getKey(), abbr.getValue());
            loadAbbreviations(); // Reload list to show the new item
        });
    }

    private void deleteSelected() {
        Abbreviation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Abbreviation: " + selected.getKey());
            alert.setContentText("Are you sure you want to permanently delete this item?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                dbManager.deleteAbbreviation(selected.getKey());
                loadAbbreviations(); // Refresh the list
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Please select an abbreviation to delete.").showAndWait();
        }
    }
}