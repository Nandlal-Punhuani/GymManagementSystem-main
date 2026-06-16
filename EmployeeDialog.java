import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class EmployeeDialog extends JDialog {
    private DatabaseConnection dbConnection;
    private Integer employeeId; // null for new employee
    private boolean dataSaved = false;
    
    private JTextField nameField, emailField, phoneField, occupationField;
    private JTextField salaryField, bonusField, workingHoursField, daysOffField;
    private JCheckBox trainerAvailableCheck;
    
    public EmployeeDialog(JFrame parent, DatabaseConnection dbConnection, Integer employeeId) {
        super(parent, employeeId == null ? "Add New Employee" : "Edit Employee", true);
        this.dbConnection = dbConnection;
        this.employeeId = employeeId;
        
        initializeComponents();
        
        if (employeeId != null) {
            loadEmployeeData();
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
        
        // Occupation
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Occupation:"), gbc);
        
        occupationField = new JTextField(20);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(occupationField, gbc);
        
        // Salary
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Salary:"), gbc);
        
        salaryField = new JTextField(20);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(salaryField, gbc);
        
        // Bonus
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Bonus:"), gbc);
        
        bonusField = new JTextField(20);
        bonusField.setText("0.00");
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(bonusField, gbc);
        
        // Working Hours
        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Working Hours:"), gbc);
        
        workingHoursField = new JTextField(20);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(workingHoursField, gbc);
        
        // Days Off
        gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Days Off:"), gbc);
        
        daysOffField = new JTextField(20);
        daysOffField.setToolTipText("Enter days separated by commas (e.g., Saturday,Sunday)");
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(daysOffField, gbc);
        
        // Trainer Available
        gbc.gridx = 0; gbc.gridy = 8; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Trainer Available:"), gbc);
        
        trainerAvailableCheck = new JCheckBox();
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(trainerAvailableCheck, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveEmployee());
        buttonPanel.add(saveButton);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadEmployeeData() {
        try {
            String query = "SELECT * FROM employees WHERE id = ?";
            ResultSet rs = dbConnection.executeQuery(query, employeeId);
            
            if (rs.next()) {
                nameField.setText(rs.getString("full_name"));
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("phone"));
                occupationField.setText(rs.getString("occupation"));
                salaryField.setText(String.valueOf(rs.getDouble("salary")));
                bonusField.setText(String.valueOf(rs.getDouble("bonus")));
                workingHoursField.setText(String.valueOf(rs.getInt("working_hours")));
                daysOffField.setText(rs.getString("days_off"));
                trainerAvailableCheck.setSelected(rs.getBoolean("trainer_available"));
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading employee data: " + e.getMessage());
        }
    }
    
    private void saveEmployee() {
        try {
            // Validate input
            if (nameField.getText().trim().isEmpty() || 
                emailField.getText().trim().isEmpty() || 
                phoneField.getText().trim().isEmpty() ||
                occupationField.getText().trim().isEmpty() ||
                salaryField.getText().trim().isEmpty() ||
                workingHoursField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
                return;
            }
            
            double salary = Double.parseDouble(salaryField.getText());
            double bonus = Double.parseDouble(bonusField.getText());
            int workingHours = Integer.parseInt(workingHoursField.getText());
            
            if (salary < 0 || bonus < 0 || workingHours < 0) {
                JOptionPane.showMessageDialog(this, "Salary, bonus, and working hours must be non-negative.");
                return;
            }
            
            if (workingHours > 168) { // 24 hours * 7 days
                JOptionPane.showMessageDialog(this, "Working hours cannot exceed 168 hours per week.");
                return;
            }
            
            if (employeeId == null) {
                // Insert new employee
                String query = "INSERT INTO employees (full_name, email, phone, occupation, " +
                              "salary, bonus, working_hours, days_off, trainer_available) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                dbConnection.executeUpdate(query, 
                    nameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    occupationField.getText().trim(),
                    salary,
                    bonus,
                    workingHours,
                    daysOffField.getText().trim(),
                    trainerAvailableCheck.isSelected());
                
                JOptionPane.showMessageDialog(this, "Employee added successfully!");
            } else {
                // Update existing employee
                String query = "UPDATE employees SET full_name = ?, email = ?, phone = ?, " +
                              "occupation = ?, salary = ?, bonus = ?, working_hours = ?, " +
                              "days_off = ?, trainer_available = ? WHERE id = ?";
                
                dbConnection.executeUpdate(query,
                    nameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    occupationField.getText().trim(),
                    salary,
                    bonus,
                    workingHours,
                    daysOffField.getText().trim(),
                    trainerAvailableCheck.isSelected(),
                    employeeId);
                
                JOptionPane.showMessageDialog(this, "Employee updated successfully!");
            }
            
            dataSaved = true;
            dispose();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for salary, bonus, and working hours.");
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("Duplicate entry")) {
                JOptionPane.showMessageDialog(this, "Email address already exists. Please use a different email.");
            } else {
                JOptionPane.showMessageDialog(this, "Error saving employee: " + e.getMessage());
            }
        }
    }
    
    public boolean isDataSaved() {
        return dataSaved;
    }
}