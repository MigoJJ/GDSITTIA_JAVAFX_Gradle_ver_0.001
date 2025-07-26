package com.ittia.gds.ui.mainframe.buttons;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * A Java Swing application to display various ICD-10 disease codes in a table.
 * The user can select a disease category from the buttons at the bottom,
 * and the table will update to show the relevant codes and descriptions.
 */
public class LoadDiseaseCodeViewer extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private final Map<String, Object[][]> diseaseDataMap = new HashMap<>();
    private final String[] columnNames = {"Code", "Disease Category"};

    /**
     * Constructor sets up the entire GUI.
     */
    public LoadDiseaseCodeViewer() {
        // --- 1. Initialize Data ---
        initializeData();

        // --- 2. Configure the Main Frame ---
        setTitle("ICD-10 Code Viewer");
        setSize(800, 600);
        setMinimumSize(new Dimension(600, 400));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 3. Create the JTable and Table Model ---
        // The table model is created with empty data initially.
        // It will be populated when a button is clicked.
        tableModel = new DefaultTableModel(new Object[][]{}, columnNames) {
            // Make cells non-editable
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);

        // --- 4. Customize Table Appearance ---
        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Set column widths
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100); // Code column
        columnModel.getColumn(0).setMaxWidth(150);
        columnModel.getColumn(1).setPreferredWidth(650); // Disease Category column

        // Add a custom renderer to make main categories BOLD
        table.getColumnModel().getColumn(1).setCellRenderer(new CategoryRenderer());


        // --- 5. Create the Scroll Pane for the Table ---
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // --- 6. Create the South Panel with Buttons ---
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        southPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Define button names
        String[] buttonLabels = {"DM", "Thyroid", "Lipid", "Osteoporosis", "URI", "Pains", "Quit"};
        ButtonClickListener buttonClickListener = new ButtonClickListener();

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setFocusPainted(false);
            button.addActionListener(buttonClickListener);
            southPanel.add(button);
        }

        add(southPanel, BorderLayout.SOUTH);

        // --- 7. Initial State ---
        // Display a welcome message in the table initially.
        showWelcomeMessage();
    }

    /**
     * Loads all the disease code data into a map for easy access.
     */
    private void initializeData() {
        // Diabetes Mellitus Data
        Object[][] dmData = {
                {"E10", "Type 1 Diabetes Mellitus"},
                {"E10.1", "  With ketoacidosis"},
                {"E10.2", "  With kidney complications (nephropathy)"},
                {"E10.3", "  With eye complications (retinopathy)"},
                {"E10.4", "  With neurological complications (neuropathy)"},
                {"E10.5", "  With peripheral circulatory complications"},
                {"E10.6", "  With other specified complications"},
                {"E10.8", "  With unspecified complications"},
                {"E10.9", "  Without complications"},
                {"", ""}, // Spacer
                {"E11", "Type 2 Diabetes Mellitus"},
                {"E11.0", "  With hyperosmolarity"},
                {"E11.1", "  With ketoacidosis"},
                {"E11.2", "  With kidney complications (nephropathy)"},
                {"E11.3", "  With eye complications (retinopathy)"},
                {"E11.4", "  With neurological complications (neuropathy)"},
                {"E11.5", "  With peripheral circulatory complications"},
                {"E11.6", "  With other specified complications"},
                {"E11.8", "  With unspecified complications"},
                {"E11.9", "  Without complications"},
                {"", ""}, // Spacer
                {"E13", "Other Specified Diabetes Mellitus"},
                {"E13.8", "  With unspecified complications"},
                {"E13.9", "  Without complications"}
        };
        diseaseDataMap.put("DM", dmData);

        // Thyroid Disorder Data
        Object[][] thyroidData = {
                {"E00–E02", "Congenital hypothyroidism"},
                {"E03", "Acquired hypothyroidism"},
                {"E03.0", "  Hashimoto’s thyroiditis"},
                {"E03.1", "  Iodine-deficiency hypothyroidism"},
                {"E03.2", "  Drug-induced hypothyroidism"},
                {"E03.9", "  Unspecified hypothyroidism"},
                {"", ""}, // Spacer
                {"E05", "Hyperthyroidism"},
                {"E05.0", "  Graves’ disease"},
                {"E05.1", "  Toxic multinodular goiter"},
                {"E05.2", "  Toxic single thyroid nodule"},
                {"E05.3", "  Drug-induced thyrotoxicosis"},
                {"", ""}, // Spacer
                {"E06", "Thyroiditis"},
                {"E06.0", "  Acute thyroiditis"},
                {"E06.1", "  Subacute (de Quervain’s) thyroiditis"},
                {"E06.2", "  Chronic autoimmune (Hashimoto’s) thyroiditis"},
                {"E06.3", "  Drug-induced thyroiditis"},
                {"", ""}, // Spacer
                {"E04", "Goiter"},
                {"E04.0", "  Nontoxic diffuse goiter"},
                {"E04.1", "  Nontoxic multinodular goiter"},
                {"E04.2", "  Nontoxic single thyroid nodule"},
                {"", ""}, // Spacer
                {"C73", "Thyroid Cancer (Malignant neoplasm)"}
        };
        diseaseDataMap.put("Thyroid", thyroidData);

        // Placeholder data for other categories
        diseaseDataMap.put("Lipid", new Object[][]{{"Info", "Lipid data not yet loaded."}});
        diseaseDataMap.put("Osteoporosis", new Object[][]{{"Info", "Osteoporosis data not yet loaded."}});
        diseaseDataMap.put("URI", new Object[][]{{"Info", "URI data not yet loaded."}});
        diseaseDataMap.put("Pains", new Object[][]{{"Info", "Pains data not yet loaded."}});
    }

    /**
     * Displays an initial message in the table before any category is selected.
     */
    private void showWelcomeMessage() {
        Object[][] welcomeData = {
            {"Welcome!", "Please select a disease category from the buttons below."}
        };
        tableModel.setDataVector(welcomeData, columnNames);
    }

    /**
     * Updates the JTable with data for the selected category.
     * @param category The key for the data map (e.g., "DM", "Thyroid").
     */
    private void updateTableData(String category) {
        Object[][] data = diseaseDataMap.get(category);
        if (data != null) {
            tableModel.setDataVector(data, columnNames);
        } else {
            // Handle case where data might be missing, though the map is pre-filled
            Object[][] errorData = {{"Error", "No data found for category: " + category}};
            tableModel.setDataVector(errorData, columnNames);
        }
        // Re-apply column widths after updating data
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100);
        columnModel.getColumn(0).setMaxWidth(150);
        columnModel.getColumn(1).setPreferredWidth(650);
    }

    /**
     * Inner class to handle all button click events.
     */
    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if ("Quit".equals(command)) {
                // Exit the application
                System.exit(0);
            } else {
                // Update the table with the corresponding data
                updateTableData(command);
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
            // Get the default component (a JLabel)
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value instanceof String) {
                String text = (String) value;
                // Main categories are not indented. Sub-categories start with spaces.
                if (!text.trim().isEmpty() && !text.startsWith("  ")) {
                    // It's a main category, make it bold.
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    // It's a sub-category or an empty spacer row, use plain font.
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
        // Run the GUI code on the Event Dispatch Thread (EDT) for thread safety.
        SwingUtilities.invokeLater(() -> {
            LoadDiseaseCodeViewer viewer = new LoadDiseaseCodeViewer();
            viewer.setLocationRelativeTo(null); // Center the frame on the screen
            viewer.setVisible(true);
        });
    }
}
