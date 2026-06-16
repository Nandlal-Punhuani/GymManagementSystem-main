import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class MembershipDialog extends JDialog {
    private DatabaseConnection dbConnection;
    private Integer membershipId; // null for new membership
    private boolean dataSaved = false;
    
    private JTextField nameField, monthlyFeeField;
    private JComboBox<String> paymentPlanCombo;
    private JTextArea equipmentAccessArea;
    private JCheckBox trainerIncludedCheck;
    
    public MembershipDialog(JFrame parent, DatabaseConnection dbConnection, Integer membershipId) {
        super(parent, membershipId == null ? "Add New Membership" : "Edit Membership", true);
        this.dbConnection = dbConnection;
        this.membershipId = membershipId;
        
        initializeComponents();
        
        if (membershipId != null) {
            loadMembershipData();
        }
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(new JLabel("Membership Name:"), gbc);
        
        nameField = new JTextField(20);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(nameField, gbc);
        
        // Payment Plan
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Payment Plan:"), gbc);
        
        paymentPlanCombo = new JComboBox<>(new String[]{"Monthly", "Quarterly", "Semi-Annual", "Annual"});
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(paymentPlanCombo, gbc);
        
        // Monthly Fee
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Monthly Fee:"), gbc);
        
        monthlyFeeField = new JTextField(20);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(monthlyFeeField, gbc);
        
        // Equipment Access
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(new JLabel("Equipment Access:"), gbc);
        
        equipmentAccessArea = new JTextArea(4, 20);
        equipmentAccessArea.setLineWrap(true);
        equipmentAccessArea.setWrapStyleWord(true);
        equipmentAccessArea.setBorder(BorderFactory.createLoweredBevelBorder());
        JScrollPane scrollPane = new JScrollPane(equipmentAccessArea);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(scrollPane, gbc);
        
        // Trainer Included
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(new JLabel("Trainer Included:"), gbc);
        
        trainerIncludedCheck = new JCheckBox();
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(trainerIncludedCheck, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveMembership());
        buttonPanel.add(saveButton);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Add some predefined equipment access examples
        if (membershipId == null) {
            equipmentAccessArea.setText("Cardio machines, Weight training area, Locker rooms");
        }
    }
    
    private void loadMembershipData() {
        try {
            String query = "SELECT * FROM membership_types WHERE id = ?";
            ResultSet rs = dbConnection.executeQuery(query, membershipId);
            
            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                paymentPlanCombo.setSelectedItem(rs.getString("payment_plan"));
                monthlyFeeField.setText(String.valueOf(rs.getDouble("monthly_fee")));
                equipmentAccessArea.setText(rs.getString("equipment_access"));
                trainerIncludedCheck.setSelected(rs.getBoolean("trainer_included"));
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading membership data: " + e.getMessage());
        }
    }
    
    private void saveMembership() {
        try {
            // Validate input
            if (nameField.getText().trim().isEmpty() || 
                monthlyFeeField.getText().trim().isEmpty() ||
                equipmentAccessArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
                return;
            }
            
            double monthlyFee = Double.parseDouble(monthlyFeeField.getText());
            
            if (monthlyFee < 0) {
                JOptionPane.showMessageDialog(this, "Monthly fee must be non-negative.");
                return;
            }
            
            if (membershipId == null) {
                // Insert new membership
                String query = "INSERT INTO membership_types (name, payment_plan, monthly_fee, " +
                              "equipment_access, trainer_included) VALUES (?, ?, ?, ?, ?)";
                
                dbConnection.executeUpdate(query, 
                    nameField.getText().trim(),
                    paymentPlanCombo.getSelectedItem(),
                    monthlyFee,
                    equipmentAccessArea.getText().trim(),
                    trainerIncludedCheck.isSelected());
                
                JOptionPane.showMessageDialog(this, "Membership added successfully!");
            } else {
                // Update existing membership
                String query = "UPDATE membership_types SET name = ?, payment_plan = ?, " +
                              "monthly_fee = ?, equipment_access = ?, trainer_included = ? " +
                              "WHERE id = ?";
                
                dbConnection.executeUpdate(query,
                    nameField.getText().trim(),
                    paymentPlanCombo.getSelectedItem(),
                    monthlyFee,
                    equipmentAccessArea.getText().trim(),
                    trainerIncludedCheck.isSelected(),
                    membershipId);
                
                JOptionPane.showMessageDialog(this, "Membership updated successfully!");
            }
            
            dataSaved = true;
            dispose();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for monthly fee.");
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("Duplicate entry")) {
                JOptionPane.showMessageDialog(this, "Membership name already exists. Please use a different name.");
            } else {
                JOptionPane.showMessageDialog(this, "Error saving membership: " + e.getMessage());
            }
        }
    }
    
    public boolean isDataSaved() {
        return dataSaved;
    }
}