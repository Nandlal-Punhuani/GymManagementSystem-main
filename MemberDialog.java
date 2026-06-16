import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MemberDialog extends JDialog {
    private DatabaseConnection dbConnection;
    private Integer memberId; // null for new member
    private boolean dataSaved = false;
    
    private JTextField nameField, emailField, phoneField, feeAmountField, finesField;
    private JComboBox<String> membershipCombo, feeStatusCombo, trainerCombo;
    private JSpinner joinDateSpinner;
    
    private Map<String, Integer> membershipMap = new HashMap<>();
    private Map<String, Integer> trainerMap = new HashMap<>();
    
    public MemberDialog(JFrame parent, DatabaseConnection dbConnection, Integer memberId) {
        super(parent, memberId == null ? "Add New Member" : "Edit Member", true);
        this.dbConnection = dbConnection;
        this.memberId = memberId;
        
        initializeComponents();
        loadComboBoxData();
        
        if (memberId != null) {
            loadMemberData();
        }
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Full Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(new JLabel("Full Name:"), gbc);
        
        nameField = new JTextField(20);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(nameField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Email:"), gbc);
        
        emailField = new JTextField(20);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(emailField, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Phone:"), gbc);
        
        phoneField = new JTextField(20);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(phoneField, gbc);
        
        // Membership Type
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Membership Type:"), gbc);
        
        membershipCombo = new JComboBox<>();
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(membershipCombo, gbc);
        
        // Fee Amount
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Fee Amount:"), gbc);
        
        feeAmountField = new JTextField(20);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(feeAmountField, gbc);
        
        // Fee Status
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Fee Status:"), gbc);
        
        feeStatusCombo = new JComboBox<>(new String[]{"Paid", "Overdue"});
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(feeStatusCombo, gbc);
        
        // Fines
        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Fines:"), gbc);
        
        finesField = new JTextField(20);
        finesField.setText("0.00");
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(finesField, gbc);
        
        // Trainer
        gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Trainer:"), gbc);
        
        trainerCombo = new JComboBox<>();
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(trainerCombo, gbc);
        
        // Join Date
        gbc.gridx = 0; gbc.gridy = 8; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Join Date:"), gbc);
        
        joinDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(joinDateSpinner, "yyyy-MM-dd");
        joinDateSpinner.setEditor(dateEditor);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(joinDateSpinner, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveMember());
        buttonPanel.add(saveButton);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadComboBoxData() {
        try {
            // Load membership types
            membershipCombo.removeAllItems();
            membershipMap.clear();
            
            ResultSet rs = dbConnection.executeQuery("SELECT id, name FROM membership_types ORDER BY name");
            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("id");
                membershipCombo.addItem(name);
                membershipMap.put(name, id);
            }
            rs.close();
            
            // Load trainers
            trainerCombo.removeAllItems();
            trainerCombo.addItem("No Trainer");
            trainerMap.clear();
            trainerMap.put("No Trainer", null);
            
            rs = dbConnection.executeQuery("SELECT id, full_name FROM employees WHERE trainer_available = TRUE ORDER BY full_name");
            while (rs.next()) {
                String name = rs.getString("full_name");
                int id = rs.getInt("id");
                trainerCombo.addItem(name);
                trainerMap.put(name, id);
            }
            rs.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }
    
    private void loadMemberData() {
        try {
            String query = "SELECT m.*, mt.name as membership_name, e.full_name as trainer_name " +
                          "FROM members m " +
                          "LEFT JOIN membership_types mt ON m.membership_type_id = mt.id " +
                          "LEFT JOIN employees e ON m.trainer_id = e.id " +
                          "WHERE m.id = ?";
            
            ResultSet rs = dbConnection.executeQuery(query, memberId);
            
            if (rs.next()) {
                nameField.setText(rs.getString("full_name"));
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("phone"));
                feeAmountField.setText(String.valueOf(rs.getDouble("fee_amount")));
                feeStatusCombo.setSelectedItem(rs.getString("fee_status"));
                finesField.setText(String.valueOf(rs.getDouble("fines")));
                joinDateSpinner.setValue(rs.getDate("join_date"));
                
                String membershipName = rs.getString("membership_name");
                if (membershipName != null) {
                    membershipCombo.setSelectedItem(membershipName);
                }
                
                String trainerName = rs.getString("trainer_name");
                if (trainerName != null) {
                    trainerCombo.setSelectedItem(trainerName);
                } else {
                    trainerCombo.setSelectedItem("No Trainer");
                }
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading member data: " + e.getMessage());
        }
    }
    
    private void saveMember() {
        try {
            // Validate input
            if (nameField.getText().trim().isEmpty() || 
                emailField.getText().trim().isEmpty() || 
                phoneField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
                return;
            }
            
            double feeAmount = Double.parseDouble(feeAmountField.getText());
            double fines = Double.parseDouble(finesField.getText());
            
            String selectedMembership = (String) membershipCombo.getSelectedItem();
            Integer membershipTypeId = membershipMap.get(selectedMembership);
            
            String selectedTrainer = (String) trainerCombo.getSelectedItem();
            Integer trainerId = trainerMap.get(selectedTrainer);
            
            java.sql.Date joinDate = new java.sql.Date(((java.util.Date) joinDateSpinner.getValue()).getTime());
            
            if (memberId == null) {
                // Insert new member
                String query = "INSERT INTO members (full_name, email, phone, membership_type_id, " +
                              "fee_amount, fee_status, fines, trainer_id, join_date) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                dbConnection.executeUpdate(query, 
                    nameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    membershipTypeId,
                    feeAmount,
                    feeStatusCombo.getSelectedItem(),
                    fines,
                    trainerId,
                    joinDate);
                
                JOptionPane.showMessageDialog(this, "Member added successfully!");
            } else {
                // Update existing member
                String query = "UPDATE members SET full_name = ?, email = ?, phone = ?, " +
                              "membership_type_id = ?, fee_amount = ?, fee_status = ?, " +
                              "fines = ?, trainer_id = ?, join_date = ? WHERE id = ?";
                
                dbConnection.executeUpdate(query,
                    nameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    membershipTypeId,
                    feeAmount,
                    feeStatusCombo.getSelectedItem(),
                    fines,
                    trainerId,
                    joinDate,
                    memberId);
                
                JOptionPane.showMessageDialog(this, "Member updated successfully!");
            }
            
            dataSaved = true;
            dispose();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for fee amount and fines.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving member: " + e.getMessage());
        }
    }
    
    public boolean isDataSaved() {
        return dataSaved;
    }
}