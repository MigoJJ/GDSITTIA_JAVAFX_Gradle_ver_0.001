package com.ittia.gds.ui.mainframe.buttons;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

/**
 * A Java Swing application to display various ICD-10 disease codes from SQLite database.
 * The user can select a disease category from the buttons at the bottom,
 * and the table will update to show the relevant codes and descriptions.
 * Enhanced with Find, Edit, Add, and Delete functionality using SQLite database.
 */
public class LoadDiseaseCodeViewer extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private final String[] columnNames = {"ID", "Code", "Disease Category", "Category"};
    private String currentCategory = null;
    private Connection connection;
    private final String DB_PATH = System.getProperty("user.dir") + "/src/main/resources/db/LoadGDScode.db";
    
    /**
     * Constructor sets up the entire GUI and database connection.
     */
    public LoadDiseaseCodeViewer() {
        // --- 1. Initialize Database ---
        initializeDatabase();
        
        // --- 2. Configure the Main Frame ---
        setTitle("ICD-10 Code Viewer with SQLite Database");
        setSize(900, 700);
        setMinimumSize(new Dimension(700, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // --- 3. Create the JTable and Table Model ---
        tableModel = new DefaultTableModel(new Object[][]{}, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable by default
            }
        };
        table = new JTable(tableModel);
        
        // Add table row sorter for filtering
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        
        // --- 4. Customize Table Appearance ---
        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set column widths
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);  // ID column
        columnModel.getColumn(0).setMaxWidth(80);
        columnModel.getColumn(1).setPreferredWidth(100); // Code column
        columnModel.getColumn(1).setMaxWidth(150);
        columnModel.getColumn(2).setPreferredWidth(500); // Disease Category column
        columnModel.getColumn(3).setPreferredWidth(100); // Category column
        columnModel.getColumn(3).setMaxWidth(120);
        
        // Add a custom renderer to make main categories BOLD
        table.getColumnModel().getColumn(2).setCellRenderer(new CategoryRenderer());
        
        // --- 5. Create the Scroll Pane for the Table ---
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        // --- 6. Create the North Panel with Find functionality ---
        JPanel northPanel = createFindPanel();
        add(northPanel, BorderLayout.NORTH);
        
        // --- 7. Create the South Panel with Buttons ---
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        southPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Define button names - added Load All, Add and Delete buttons
        String[] buttonLabels = {"Load All", "DM", "Thyroid", "Lipid", "Osteoporosis", "URI", "Pains", "Add", "Edit", "Delete", "Quit"};
        ButtonClickListener buttonClickListener = new ButtonClickListener();
        
        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setFocusPainted(false);
            button.addActionListener(buttonClickListener);
            
            // Style special buttons differently
            if ("Load All".equals(label)) {
                button.setBackground(new Color(173, 216, 230)); // Light blue
                button.setOpaque(true);
            } else if ("Add".equals(label)) {
                button.setBackground(new Color(144, 238, 144)); // Light green
                button.setOpaque(true);
            } else if ("Edit".equals(label)) {
                button.setBackground(new Color(255, 215, 0)); // Gold
                button.setOpaque(true);
            } else if ("Delete".equals(label)) {
                button.setBackground(new Color(255, 182, 193)); // Light pink
                button.setOpaque(true);
            }
            
            southPanel.add(button);
        }
        
        add(southPanel, BorderLayout.SOUTH);
        
        // --- 8. Initial State ---
        loadAllData(); // Load all data initially instead of welcome message
        
        // --- 9. Add window closing event to close database connection ---
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                closeDatabase();
                System.exit(0);
            }
        });
    }
    
    /**
     * Initialize SQLite database connection and create table if not exists
     */
    private void initializeDatabase() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Ensure database directory exists
            java.io.File dbFile = new java.io.File(DB_PATH);
            java.io.File dbDir = dbFile.getParentFile();
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }
            
            // Create connection to database
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
            
            // Create table if it doesn't exist
            createTableIfNotExists();
            
            // Insert sample data if table is empty
            insertSampleDataIfEmpty();
            
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, 
                "SQLite JDBC driver not found: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Database connection error: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Create the disease_codes table if it doesn't exist
     */
    private void createTableIfNotExists() throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS disease_codes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                code TEXT NOT NULL,
                description TEXT NOT NULL,
                category TEXT NOT NULL
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
        }
    }
    
    /**
     * Insert sample data if the table is empty
     */
    private void insertSampleDataIfEmpty() throws SQLException {
        String countSQL = "SELECT COUNT(*) FROM disease_codes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(countSQL)) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                insertSampleData();
            }
        }
    }
    
    /**
     * Insert sample disease code data
     */
    private void insertSampleData() throws SQLException {
        String insertSQL = "INSERT INTO disease_codes (code, description, category) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            // Diabetes Mellitus Data
            String[][] dmData = {
            		{"E10", "Type 1 Diabetes Mellitus", "DM"},
            		{"E10.9", "    Type 1 Diabetes Mellitus Without complications", "DM"},
            		{"E10.1", "    Type 1 Diabetes Mellitus With ketoacidosis", "DM"},
            		{"E10.2", "    Type 1 Diabetes Mellitus With kidney complications (nephropathy)", "DM"},
            		{"E10.3", "    Type 1 Diabetes Mellitus With ophthalmic complications", "DM"},
            		{"E10.4", "    Type 1 Diabetes Mellitus With neurological complications", "DM"},
            		{"E10.5", "    Type 1 Diabetes Mellitus With circulatory complications", "DM"},
            		{"E10.6", "    Type 1 Diabetes Mellitus With other specified complications", "DM"},
            		{"E10.8", "    Type 1 Diabetes Mellitus With unspecified complications", "DM"},

            		{"E11", "Type 2 Diabetes Mellitus", "DM"},
            		{"E11.9", "    Type 2 Diabetes Mellitus Without complications", "DM"},
            		{"E11.2", "    Type 2 Diabetes Mellitus With kidney complications (nephropathy)", "DM"},
            		{"E11.3", "    Type 2 Diabetes Mellitus With ophthalmic complications", "DM"},
            		{"E11.4", "    Type 2 Diabetes Mellitus With neurological complications", "DM"},
            		{"E11.5", "    Type 2 Diabetes Mellitus With circulatory complications", "DM"},
            		{"E11.6", "    Type 2 Diabetes Mellitus With other specified complications", "DM"},
            		{"E11.8", "    Type 2 Diabetes Mellitus With unspecified complications", "DM"},

            		{"E12", "Malnutrition-related Diabetes Mellitus", "DM"},
            		{"E13", "Other specified Diabetes Mellitus", "DM"},
            		{"E08", "Diabetes Mellitus Due to underlying condition", "DM"},

            		{"O24", "Diabetes Mellitus in pregnancy, childbirth, and puerperium", "DM"},
            		{"O24.0", "    Pre-existing Type 1 Diabetes Mellitus in pregnancy", "DM"},
            		{"O24.1", "    Pre-existing Type 2 Diabetes Mellitus in pregnancy", "DM"},
            		{"O24.4", "    Gestational Diabetes Mellitus", "DM"},

            		{"E00", "Congenital iodine-deficiency syndrome", "Thyroid"},
            		{"E01", "Iodine-deficiency related thyroid disorders", "Thyroid"},
            		{"E03", "Other hypothyroidism", "Thyroid"},
            		{"E03.9", "    Hypothyroidism, unspecified", "Thyroid"},
            		{"E03.0", "    Congenital hypothyroidism with diffuse goiter", "Thyroid"},
            		{"E03.1", "    Congenital hypothyroidism without goiter", "Thyroid"},

            		{"E05", "Thyrotoxicosis (hyperthyroidism)", "Thyroid"},
            		{"E05.0", "    Thyrotoxicosis with diffuse goiter (Graves’ disease)", "Thyroid"},
            		{"E05.9", "    Thyrotoxicosis, unspecified", "Thyroid"},

            		{"E06", "Thyroiditis", "Thyroid"},
            		{"E06.3", "    Autoimmune thyroiditis (Hashimoto’s thyroiditis)", "Thyroid"},

            		{"E07", "Other disorders of thyroid", "Thyroid"},
            		{"E07.9", "    Disorder of thyroid, unspecified", "Thyroid"},
            		{"E07.1", "Dyshormogenetic goiter", "Thyroid"},

            		{"E78", "Disorders of lipoprotein metabolism and other lipidemias", "Lipid"},
            		{"E78.0", "    Pure hypercholesterolemia", "Lipid"},
            		{"E78.1", "    Pure hyperglyceridemia", "Lipid"},
            		{"E78.2", "    Mixed hyperlipidemia", "Lipid"},
            		{"E78.5", "    Hyperlipidemia, unspecified", "Lipid"},
            		{"E78.6", "    Lipoprotein deficiency (HDL deficiency)", "Lipid"},
            		{"E78.9", "    Disorder of lipoprotein metabolism, unspecified", "Lipid"},

            		{"M80", "Osteoporosis with current pathological fracture", "Osteoporosis"},
            		{"M80.0", "    Age-related osteoporosis with current pathological fracture", "Osteoporosis"},
            		{"M80.8", "    Other osteoporosis with current pathological fracture", "Osteoporosis"},

            		{"M81", "Osteoporosis without pathological fracture", "Osteoporosis"},
            		{"M81.0", "    Age-related osteoporosis without current pathological fracture", "Osteoporosis"},
            		{"M81.8", "    Other osteoporosis without current pathological fracture", "Osteoporosis"},

            		{"M82", "Osteoporosis in diseases classified elsewhere (secondary)", "Osteoporosis"},

            		{"E20", "Hypoparathyroidism", "Endocrine"},
            		{"E21", "Hyperparathyroidism and other disorders of parathyroid gland", "Endocrine"},
            		{"E22", "Hyperfunction of pituitary gland", "Endocrine"},
            		{"E23", "Hypofunction and other disorders of pituitary gland", "Endocrine"},
            		{"E27", "Other disorders of adrenal gland (Addison’s, Cushing's)", "Endocrine"},
            		{"E31", "Polyglandular dysfunction", "Endocrine"},
            		{"E31.0", "    Autoimmune polyglandular failure (Schmidt’s syndrome)", "Endocrine"},
            		{"E31.9", "    Polyglandular dysfunction, unspecified", "Endocrine"},
            		{"E34", "Other endocrine disorders", "Endocrine"},
            		{"E34.9", "    Endocrine disorder, unspecified", "Endocrine"},

            		{"E89", "Postprocedural endocrine and metabolic complications", "Endocrine"},
            		{"E89.0", "    Postprocedural hypothyroidism", "Endocrine"},

            };
            
            // Thyroid Data
            String[][] thyroidData = {

                {"C73", "Thyroid Cancer (Malignant neoplasm)", "Thyroid"}
            };
            
            // Insert DM data
            for (String[] row : dmData) {
                pstmt.setString(1, row[0]);
                pstmt.setString(2, row[1]);
                pstmt.setString(3, row[2]);
                pstmt.executeUpdate();
            }
            
            // Insert Thyroid data
            for (String[] row : thyroidData) {
                pstmt.setString(1, row[0]);
                pstmt.setString(2, row[1]);
                pstmt.setString(3, row[2]);
                pstmt.executeUpdate();
            }
            
            // Insert placeholder data for other categories
            String[][] otherData = {
                {"Info", "Lipid data not yet loaded.", "Lipid"},
                {"Info", "Osteoporosis data not yet loaded.", "Osteoporosis"},
                {"Info", "URI data not yet loaded.", "URI"},
                {"Info", "Pains data not yet loaded.", "Pains"}
            };
            
            for (String[] row : otherData) {
                pstmt.setString(1, row[0]);
                pstmt.setString(2, row[1]);
                pstmt.setString(3, row[2]);
                pstmt.executeUpdate();
            }
        }
    }
    
    /**
     * Creates the find panel with search functionality
     */
    private JPanel createFindPanel() {
        JPanel findPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        findPanel.setBorder(BorderFactory.createTitledBorder("Search"));
        
        JLabel findLabel = new JLabel("Find:");
        JTextField findField = new JTextField(20);
        JButton findButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");
        
        findButton.addActionListener(e -> performFind(findField.getText()));
        clearButton.addActionListener(e -> {
            findField.setText("");
            sorter.setRowFilter(null);
        });
        
        // Allow Enter key to trigger search
        findField.addActionListener(e -> performFind(findField.getText()));
        
        findPanel.add(findLabel);
        findPanel.add(findField);
        findPanel.add(findButton);
        findPanel.add(clearButton);
        
        return findPanel;
    }
    
    /**
     * Performs the find operation by filtering table rows
     */
    private void performFind(String searchText) {
        if (searchText.trim().isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        
        try {
            RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + searchText);
            sorter.setRowFilter(rf);
            
            if (table.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, 
                    "No results found for: " + searchText, 
                    "Search Results", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PatternSyntaxException ex) {
            JOptionPane.showMessageDialog(this, 
                "Invalid search pattern: " + ex.getMessage(), 
                "Search Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Opens the add dialog for creating new disease codes
     */
    private void openAddDialog() {
        JDialog addDialog = new JDialog(this, "Add New Disease Code", true);
        addDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Code field
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 5, 5);
        addDialog.add(new JLabel("Code:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 5, 5, 10);
        JTextField codeField = new JTextField(20);
        addDialog.add(codeField, gbc);
        
        // Description field
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.insets = new Insets(5, 10, 5, 5);
        addDialog.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 10);
        JTextField descField = new JTextField(30);
        addDialog.add(descField, gbc);
        
        // Category field
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.insets = new Insets(5, 10, 5, 5);
        addDialog.add(new JLabel("Category:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 10);
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"DM", "Thyroid", "Lipid", "Osteoporosis", "URI", "Pains"});
        if (currentCategory != null) {
            categoryCombo.setSelectedItem(currentCategory);
        }
        addDialog.add(categoryCombo, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER; gbc.weightx = 0;
        gbc.insets = new Insets(15, 10, 10, 10);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            String code = codeField.getText().trim();
            String description = descField.getText().trim();
            String category = (String) categoryCombo.getSelectedItem();
            
            if (code.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(addDialog, 
                    "Please fill in all fields.", 
                    "Validation Error", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (addDiseaseCode(code, description, category)) {
                addDialog.dispose();
                if ("All".equals(currentCategory)) {
                    loadAllData(); // Refresh all data view
                } else if (category.equals(currentCategory)) {
                    loadDataByCategory(currentCategory); // Refresh current category view
                }
                JOptionPane.showMessageDialog(this, 
                    "Disease code added successfully!", 
                    "Add Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> addDialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        addDialog.add(buttonPanel, gbc);
        
        addDialog.setSize(400, 200);
        addDialog.setLocationRelativeTo(this);
        addDialog.setVisible(true);
    }
    
    /**
     * Opens the edit dialog for modifying disease codes
     */
    private void openEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a row to edit.", 
                "No Row Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = (Integer) tableModel.getValueAt(modelRow, 0);
        String currentCode = (String) tableModel.getValueAt(modelRow, 1);
        String currentDescription = (String) tableModel.getValueAt(modelRow, 2);
        String currentCat = (String) tableModel.getValueAt(modelRow, 3);
        
        JDialog editDialog = new JDialog(this, "Edit Disease Code", true);
        editDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Code field
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 5, 5);
        editDialog.add(new JLabel("Code:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 5, 5, 10);
        JTextField codeField = new JTextField(currentCode, 20);
        editDialog.add(codeField, gbc);
        
        // Description field
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.insets = new Insets(5, 10, 5, 5);
        editDialog.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 10);
        JTextField descField = new JTextField(currentDescription, 30);
        editDialog.add(descField, gbc);
        
        // Category field
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.insets = new Insets(5, 10, 5, 5);
        editDialog.add(new JLabel("Category:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 10);
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"DM", "Thyroid", "Lipid", "Osteoporosis", "URI", "Pains"});
        categoryCombo.setSelectedItem(currentCat);
        editDialog.add(categoryCombo, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER; gbc.weightx = 0;
        gbc.insets = new Insets(15, 10, 10, 10);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            String code = codeField.getText().trim();
            String description = descField.getText().trim();
            String category = (String) categoryCombo.getSelectedItem();
            
            if (code.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(editDialog, 
                    "Please fill in all fields.", 
                    "Validation Error", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (updateDiseaseCode(id, code, description, category)) {
                editDialog.dispose();
                if ("All".equals(currentCategory)) {
                    loadAllData(); // Refresh all data view
                } else if (category.equals(currentCategory)) {
                    loadDataByCategory(currentCategory); // Refresh current category view
                }
                JOptionPane.showMessageDialog(this, 
                    "Changes saved successfully!", 
                    "Save Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> editDialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        editDialog.add(buttonPanel, gbc);
        
        editDialog.setSize(400, 200);
        editDialog.setLocationRelativeTo(this);
        editDialog.setVisible(true);
    }
    
    /**
     * Deletes the selected disease code
     */
    private void deleteSelectedRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a row to delete.", 
                "No Row Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = (Integer) tableModel.getValueAt(modelRow, 0);
        String code = (String) tableModel.getValueAt(modelRow, 1);
        String description = (String) tableModel.getValueAt(modelRow, 2);
        
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this record?\n\n" +
            "Code: " + code + "\nDescription: " + description,
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            if (deleteDiseaseCode(id)) {
                if ("All".equals(currentCategory)) {
                    loadAllData(); // Refresh all data view
                } else {
                    loadDataByCategory(currentCategory); // Refresh current category view
                }
                JOptionPane.showMessageDialog(this, 
                    "Record deleted successfully!", 
                    "Delete Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    /**
     * Add a new disease code to the database
     */
    private boolean addDiseaseCode(String code, String description, String category) {
        String insertSQL = "INSERT INTO disease_codes (code, description, category) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, code);
            pstmt.setString(2, description);
            pstmt.setString(3, category);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error adding disease code: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Update an existing disease code in the database
     */
    private boolean updateDiseaseCode(int id, String code, String description, String category) {
        String updateSQL = "UPDATE disease_codes SET code = ?, description = ?, category = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            pstmt.setString(1, code);
            pstmt.setString(2, description);
            pstmt.setString(3, category);
            pstmt.setInt(4, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error updating disease code: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Delete a disease code from the database
     */
    private boolean deleteDiseaseCode(int id) {
        String deleteSQL = "DELETE FROM disease_codes WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(deleteSQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error deleting disease code: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Load all data from database
     */
    private void loadAllData() {
        String selectSQL = "SELECT id, code, description, category FROM disease_codes ORDER BY category, id";
        
        try (PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            ResultSet rs = pstmt.executeQuery();
            
            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("code"));
                row.add(rs.getString("description"));
                row.add(rs.getString("category"));
                data.add(row);
            }
            
            Vector<String> columns = new Vector<>();
            for (String col : columnNames) {
                columns.add(col);
            }
            
            tableModel.setDataVector(data, columns);
            currentCategory = "All"; // Set current category to "All"
            
            // Clear any existing filters
            sorter.setRowFilter(null);
            
            // Re-apply column widths
            TableColumnModel columnModel = table.getColumnModel();
            columnModel.getColumn(0).setPreferredWidth(50);
            columnModel.getColumn(0).setMaxWidth(80);
            columnModel.getColumn(1).setPreferredWidth(100);
            columnModel.getColumn(1).setMaxWidth(150);
            columnModel.getColumn(2).setPreferredWidth(500);
            columnModel.getColumn(3).setPreferredWidth(100);
            columnModel.getColumn(3).setMaxWidth(120);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading all data: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * Load data from database by category
     */
    private void loadDataByCategory(String category) {
        String selectSQL = "SELECT id, code, description, category FROM disease_codes WHERE category = ? ORDER BY id";
        
        try (PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();
            
            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("code"));
                row.add(rs.getString("description"));
                row.add(rs.getString("category"));
                data.add(row);
            }
            
            Vector<String> columns = new Vector<>();
            for (String col : columnNames) {
                columns.add(col);
            }
            
            tableModel.setDataVector(data, columns);
            currentCategory = category;
            
            // Clear any existing filters
            sorter.setRowFilter(null);
            
            // Re-apply column widths
            TableColumnModel columnModel = table.getColumnModel();
            columnModel.getColumn(0).setPreferredWidth(50);
            columnModel.getColumn(0).setMaxWidth(80);
            columnModel.getColumn(1).setPreferredWidth(100);
            columnModel.getColumn(1).setMaxWidth(150);
            columnModel.getColumn(2).setPreferredWidth(500);
            columnModel.getColumn(3).setPreferredWidth(100);
            columnModel.getColumn(3).setMaxWidth(120);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading data: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Displays an initial message in the table before any category is selected.
     */
    private void showWelcomeMessage() {
        Vector<Vector<Object>> welcomeData = new Vector<>();
        Vector<Object> row = new Vector<>();
        row.add(0);
        row.add("Welcome!");
        row.add("Please select a disease category from the buttons below.");
        row.add("System");
        welcomeData.add(row);
        
        Vector<String> columns = new Vector<>();
        for (String col : columnNames) {
            columns.add(col);
        }
        
        tableModel.setDataVector(welcomeData, columns);
        currentCategory = null;
    }
    
    /**
     * Close database connection
     */
    private void closeDatabase() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }
    
    /**
     * Inner class to handle all button click events.
     */
    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if ("Quit".equals(command)) {
                int result = JOptionPane.showConfirmDialog(
                    LoadDiseaseCodeViewer.this,
                    "Are you sure you want to quit?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                if (result == JOptionPane.YES_OPTION) {
                    closeDatabase();
                    System.exit(0);
                }
            } else if ("Load All".equals(command)) {
                loadAllData();
            } else if ("Add".equals(command)) {
                openAddDialog();
            } else if ("Edit".equals(command)) {
                openEditDialog();
            } else if ("Delete".equals(command)) {
                deleteSelectedRow();
            } else {
                // Load data for the selected category
                loadDataByCategory(command);
            }
        }
    }
    
    /**
     * Custom cell renderer to format the category column.
     * It makes the text BOLD for main categories (which have no indentation).
     */
    private static class CategoryRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof String) {
                String text = (String) value;
                // Main categories are not indented. Sub-categories start with spaces.
                if (!text.trim().isEmpty() && !text.startsWith("  ")) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }
            }
            return c;
        }
    }
    
    /**
     * Main method to run the application.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoadDiseaseCodeViewer viewer = new LoadDiseaseCodeViewer();
            viewer.setLocationRelativeTo(null);
            viewer.setVisible(true);
        });
    }
}