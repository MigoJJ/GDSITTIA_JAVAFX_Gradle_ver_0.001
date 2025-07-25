package com.ittia.gds.ui.mainframe.changestring;

import java.util.Map;
import java.util.Optional;

import com.ittia.gds.db.DatabaseManager; // Import the new DatabaseManager
import com.ittia.gds.ui.model.Abbreviation;

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
import javafx.scene.layout.Priority; // Import Priority
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AbbreviationManagerUI {

    private final DatabaseManager dbManager;
    private final AbbreviationsMain abbreviationHandler; // Used to tell the main handler to refresh its map
    private final TableView<Abbreviation> tableView = new TableView<>();
    private final ObservableList<Abbreviation> abbreviationList = FXCollections.observableArrayList();
    private FilteredList<Abbreviation> filteredData; 

    public AbbreviationManagerUI(AbbreviationsMain handler, DatabaseManager dbManager) {
        this.abbreviationHandler = handler;
        this.dbManager = dbManager;
    }

    public void display() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Abbreviation Manager");
        stage.setMinWidth(650);
        stage.setMinHeight(450);

        // --- Table View Setup ---
        TableColumn<Abbreviation, String> keyCol = new TableColumn<>("Key");
        keyCol.setCellValueFactory(new PropertyValueFactory<>("key"));
        keyCol.setPrefWidth(150);

        TableColumn<Abbreviation, String> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueCol.setPrefWidth(350); // Give more space for value

        tableView.getColumns().addAll(keyCol, valueCol);
        
        filteredData = new FilteredList<>(abbreviationList, p -> true); // Initialize here
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

        // Clear filter button
        Button clearFilterButton = new Button("X");
        clearFilterButton.setOnAction(e -> findField.clear());
        
        HBox searchBox = new HBox(5, findField, clearFilterButton);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(findField, Priority.ALWAYS);

        Button addButton = new Button("Add...");
        addButton.setOnAction(e -> showAddEditDialog(null));

        Button editButton = new Button("Edit...");
        editButton.setOnAction(e -> editSelected());

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteSelected());

        Button quitButton = new Button("Quit");
        quitButton.setOnAction(e -> stage.close());

        HBox controls = new HBox(10, searchBox, addButton, editButton, deleteButton, quitButton);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchBox, Priority.ALWAYS);


        // --- Double-click to Edit ---
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tableView.getSelectionModel().getSelectedItem() != null) {
                editSelected();
            }
        });

        // --- Layout ---
        BorderPane layout = new BorderPane();
        layout.setCenter(tableView);
        layout.setBottom(controls);

        Scene scene = new Scene(layout, 650, 450); // Adjust size for better fit
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void loadAbbreviations() {
        abbreviationList.clear();
        Map<String, String> allAbbrs = dbManager.getAllAbbreviations(); // Use DatabaseManager
        allAbbrs.forEach((key, value) -> abbreviationList.add(new Abbreviation(key, value)));
        abbreviationHandler.refreshAbbreviationsMap(); // Notify the main handler to refresh its map
    }
    
    private void showAddEditDialog(Abbreviation abbrToEdit) {
        Dialog<Abbreviation> dialog = new Dialog<>();
        dialog.setTitle(abbrToEdit == null ? "Add New Abbreviation" : "Edit Abbreviation");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField keyField = new TextField();
        keyField.setPromptText("Key (e.g., :htn )");
        TextField valueField = new TextField();
        valueField.setPromptText("Value (e.g., Hypertension)");

        if (abbrToEdit != null) {
            keyField.setText(abbrToEdit.getKey());
            valueField.setText(abbrToEdit.getValue());
        }

        grid.add(new Label("Key:"), 0, 0);
        grid.add(keyField, 1, 0);
        grid.add(new Label("Value:"), 0, 1);
        grid.add(valueField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        // Enable/Disable Save button based on input
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(keyField.getText().isBlank() || valueField.getText().isBlank());

        keyField.textProperty().addListener((obs, oldVal, newVal) -> 
            saveButton.setDisable(newVal.isBlank() || valueField.getText().isBlank()));
        valueField.textProperty().addListener((obs, oldVal, newVal) -> 
            saveButton.setDisable(keyField.getText().isBlank() || newVal.isBlank()));


        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (!keyField.getText().isBlank() && !valueField.getText().isBlank()) {
                    String formattedKey = keyField.getText().trim();
                    if (!formattedKey.startsWith(":")) {
                        formattedKey = ":" + formattedKey;
                    }
                    if (!formattedKey.endsWith(" ")) {
                        formattedKey = formattedKey + " ";
                    }
                    return new Abbreviation(formattedKey, valueField.getText().trim());
                } else {
                    new Alert(Alert.AlertType.WARNING, "Key and Value cannot be empty.").showAndWait();
                    return null;
                }
            }
            return null;
        });

        Optional<Abbreviation> result = dialog.showAndWait();
        result.ifPresent(abbr -> {
            // Check if the key already exists and warn/confirm if it's an "add" operation
            // and the key is different from the original (if editing)
            if (abbrToEdit == null && dbManager.getAllAbbreviations().containsKey(abbr.getKey())) {
                Alert confirmOverwrite = new Alert(Alert.AlertType.CONFIRMATION);
                confirmOverwrite.setTitle("Confirm Overwrite");
                confirmOverwrite.setHeaderText("Abbreviation '" + abbr.getKey() + "' already exists.");
                confirmOverwrite.setContentText("Do you want to update its value?");
                Optional<ButtonType> confirmResult = confirmOverwrite.showAndWait();
                if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                    dbManager.addOrUpdateAbbreviation(abbr.getKey(), abbr.getValue()); // Use DatabaseManager
                    loadAbbreviations();
                }
            } else {
                dbManager.addOrUpdateAbbreviation(abbr.getKey(), abbr.getValue()); // Use DatabaseManager
                loadAbbreviations();
            }
        });
    }

    private void editSelected() {
        Abbreviation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showAddEditDialog(selected);
        } else {
            new Alert(Alert.AlertType.WARNING, "Please select an abbreviation to edit.").showAndWait();
        }
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
                dbManager.deleteAbbreviation(selected.getKey()); // Use DatabaseManager
                loadAbbreviations();
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Please select an abbreviation to delete.").showAndWait();
        }
    }
}