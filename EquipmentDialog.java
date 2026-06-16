import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class EquipmentDialog extends JDialog {
    private DatabaseConnection dbConnection;
    private Integer equipmentId;
    private boolean dataSaved = false;
    
    private JTextField equipmentIdField;
    private JTextField nameField;
    private JTextField worthField;
    private JComboBox<String> conditionCombo;
    private JLabel maintenanceLabel;
    private JLabel maintenanceValueLabel;
    
    public EquipmentDialog(JFrame parent, DatabaseConnection dbConnection, Integer equipmentId) {
        super(parent, equipmentId == null ? "Add Equipment" : "Edit Equipment", true);
        this.dbConnection = dbConnection;
        this.equipmentId = equipmentId;
        
        initializeComponents();
        
        if (equipmentId != null) {
            loadEquipmentData();
        }
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Equipment ID
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Equipment ID:"), gbc);
        gbc.gridx = 1;
        equipmentIdField = new JTextField(15);
        mainPanel.add(equipmentIdField, gbc);
        
        // Name
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(15);
        mainPanel.add(nameField, gbc);
        
        // Worth
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Worth ($):"), gbc);
        gbc.gridx = 1;
        worthField = new JTextField(15);
        mainPanel.add(worthField, gbc);
        
        // Condition
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Condition:"), gbc);
        gbc.gridx = 1;
        String[] conditions = {"Excellent", "Good", "Poor", "Damaged"};
        conditionCombo = new JComboBox<>(conditions);
        conditionCombo.addActionListener(e -> updateMaintenanceRequired());
        mainPanel.add(conditionCombo, gbc);
        
        // Maintenance Required (read-only)
        gbc.gridx = 0; gbc.gridy = 4;
        maintenanceLabel = new JLabel("Action Required:");
        mainPanel.add(maintenanceLabel, gbc);
        gbc.gridx = 1;
        maintenanceValueLabel = new JLabel("No");
        maintenanceValueLabel.setOpaque(true);
        maintenanceValueLabel.setBackground(new Color(200, 255, 200));
        maintenanceValueLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        mainPanel.add(maintenanceValueLabel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveEquipment());
        buttonPanel.add(saveButton);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Initialize maintenance required
        updateMaintenanceRequired();
        
        // Set default focus
        SwingUtilities.invokeLater(() -> equipmentIdField.requestFocus());
    }
    
    private void updateMaintenanceRequired() {
        String condition = (String) conditionCombo.getSelectedItem();
        String maintenanceRequired;
        Color backgroundColor;
        
        switch (condition) {
            case "Excellent":
            case "Good":
                maintenanceRequired = "No";
                backgroundColor = new Color(200, 255, 200); // Light green
                break;
            case "Poor":
                maintenanceRequired = "Maintenance";
                backgroundColor = new Color(255, 220, 200); // Light orange
                break;
            case "Damaged":
                maintenanceRequired = "Replacement";
                backgroundColor = new Color(255, 200, 200); // Light red
                break;
            default:
                maintenanceRequired = "No";
                backgroundColor = Color.WHITE;
        }
        
        maintenanceValueLabel.setText(maintenanceRequired);
        maintenanceValueLabel.setBackground(backgroundColor);
    }
    
    private void loadEquipmentData() {
        try {
            String query = "SELECT * FROM gym_equipment WHERE id = ?";
            ResultSet rs = dbConnection.executeQuery(query, equipmentId);
            
            if (rs.next()) {
                equipmentIdField.setText(rs.getString("equipment_id"));
                nameField.setText(rs.getString("name"));
                worthField.setText(String.valueOf(rs.getDouble("worth")));
                conditionCombo.setSelectedItem(rs.getString("condition_status"));
                updateMaintenanceRequired();
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading equipment data: " + e.getMessage());
        }
    }
    
    private void saveEquipment() {
        // Validate input
        if (equipmentIdField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Equipment ID is required.");
            equipmentIdField.requestFocus();
            return;
        }
        
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Equipment name is required.");
            nameField.requestFocus();
            return;
        }
        
        double worth;
        try {
            worth = Double.parseDouble(worthField.getText().trim());
            if (worth < 0) {
                JOptionPane.showMessageDialog(this, "Worth must be a positive number.");
                worthField.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid worth amount.");
            worthField.requestFocus();
            return;
        }
        
        try {
            String equipmentIdValue = equipmentIdField.getText().trim();
            String name = nameField.getText().trim();
            String condition = (String) conditionCombo.getSelectedItem();
            String maintenanceRequired = maintenanceValueLabel.getText();
            
            if (equipmentId == null) {
                // Check if equipment ID already exists
                String checkQuery = "SELECT COUNT(*) FROM gym_equipment WHERE equipment_id = ?";
                ResultSet rs = dbConnection.executeQuery(checkQuery, equipmentIdValue);
                rs.next();
                if (rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Equipment ID already exists. Please use a different ID.");
                    equipmentIdField.requestFocus();
                    rs.close();
                    return;
                }
                rs.close();
                
                // Insert new equipment
                String insertQuery = "INSERT INTO gym_equipment (equipment_id, name, worth, condition_status, maintenance_required) " +
                                   "VALUES (?, ?, ?, ?, ?)";
                dbConnection.executeUpdate(insertQuery, equipmentIdValue, name, worth, condition, maintenanceRequired);
                JOptionPane.showMessageDialog(this, "Equipment added successfully!");
            } else {
                // Check if equipment ID already exists for other equipment
                String checkQuery = "SELECT COUNT(*) FROM gym_equipment WHERE equipment_id = ? AND id != ?";
                ResultSet rs = dbConnection.executeQuery(checkQuery, equipmentIdValue, equipmentId);
                rs.next();
                if (rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Equipment ID already exists. Please use a different ID.");
                    equipmentIdField.requestFocus();
                    rs.close();
                    return;
                }
                rs.close();
                
                // Update existing equipment
                String updateQuery = "UPDATE gym_equipment SET equipment_id = ?, name = ?, worth = ?, " +
                                   "condition_status = ?, maintenance_required = ? WHERE id = ?";
                dbConnection.executeUpdate(updateQuery, equipmentIdValue, name, worth, condition, 
                                         maintenanceRequired, equipmentId);
                JOptionPane.showMessageDialog(this, "Equipment updated successfully!");
            }
            
            dataSaved = true;
            dispose();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving equipment: " + e.getMessage());
        }
    }
    
    public boolean isDataSaved() {
        return dataSaved;
    }
}