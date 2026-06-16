import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FinancialDialog extends JDialog {
    private DatabaseConnection dbConnection;
    private String recordType; // "investment", "earning", or "expense"
    private Integer recordId;
    private boolean dataSaved = false;
    
    // Common fields
    private JTextField amountField;
    private JTextArea descriptionArea;
    private JSpinner dateSpinner;
    
    // Investment specific fields
    private JTextField investorNameField;
    private JTextField roiField;
    
    // Earning specific fields
    private JComboBox<String> earningSourceCombo;
    
    // Expense specific fields
    private JComboBox<String> expenseCategoryCombo;
    
    public FinancialDialog(JFrame parent, DatabaseConnection dbConnection, String recordType, Integer recordId) {
        super(parent, getDialogTitle(recordType, recordId), true);
        this.dbConnection = dbConnection;
        this.recordType = recordType;
        this.recordId = recordId;
        
        initializeComponents();
        
        if (recordId != null) {
            loadRecordData();
        }
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private static String getDialogTitle(String recordType, Integer recordId) {
        String action = recordId == null ? "Add" : "Edit";
        String type = recordType.substring(0, 1).toUpperCase() + recordType.substring(1);
        return action + " " + type;
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Type-specific fields
        if ("investment".equals(recordType)) {
            // Investor Name
            gbc.gridx = 0; gbc.gridy = row;
            mainPanel.add(new JLabel("Investor Name:"), gbc);
            gbc.gridx = 1;
            investorNameField = new JTextField(20);
            mainPanel.add(investorNameField, gbc);
            row++;
        } else if ("earning".equals(recordType)) {
            // Earning Source
            gbc.gridx = 0; gbc.gridy = row;
            mainPanel.add(new JLabel("Source:"), gbc);
            gbc.gridx = 1;
            String[] earningSources = {"Membership Fees", "Product Sales", "Fines", "Donations", "Other"};
            earningSourceCombo = new JComboBox<>(earningSources);
            earningSourceCombo.setEditable(true);
            mainPanel.add(earningSourceCombo, gbc);
            row++;
        } else if ("expense".equals(recordType)) {
            // Expense Category
            gbc.gridx = 0; gbc.gridy = row;
            mainPanel.add(new JLabel("Category:"), gbc);
            gbc.gridx = 1;
            String[] expenseCategories = {"Salaries", "Maintenance", "Equipment", "Utilities", 
                                         "Tax", "ROI Payment", "Rent", "Insurance", "Other"};
            expenseCategoryCombo = new JComboBox<>(expenseCategories);
            expenseCategoryCombo.setEditable(true);
            mainPanel.add(expenseCategoryCombo, gbc);
            row++;
        }
        
        // Amount
        gbc.gridx = 0; gbc.gridy = row;
        mainPanel.add(new JLabel("Amount ($):"), gbc);
        gbc.gridx = 1;
        amountField = new JTextField(20);
        mainPanel.add(amountField, gbc);
        row++;
        
        // ROI Percentage (only for investments)
        if ("investment".equals(recordType)) {
            gbc.gridx = 0; gbc.gridy = row;
            mainPanel.add(new JLabel("ROI Percentage:"), gbc);
            gbc.gridx = 1;
            roiField = new JTextField(20);
            mainPanel.add(roiField, gbc);
            row++;
        }
        
        // Description
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        mainPanel.add(descScrollPane, gbc);
        row++;
        
        // Date
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1;
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new Date()); // Set to current date
        dateSpinner.setPreferredSize(new Dimension(200, 25));
        mainPanel.add(dateSpinner, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveRecord());
        buttonPanel.add(saveButton);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set default focus
        SwingUtilities.invokeLater(() -> {
            if ("investment".equals(recordType)) {
                investorNameField.requestFocus();
            } else {
                amountField.requestFocus();
            }
        });
    }
    
    private void loadRecordData() {
        try {
            String query = "";
            String tableName = "";
            
            switch (recordType) {
                case "investment":
                    tableName = "investments";
                    query = "SELECT * FROM investments WHERE id = ?";
                    break;
                case "earning":
                    tableName = "earnings";
                    query = "SELECT * FROM earnings WHERE id = ?";
                    break;
                case "expense":
                    tableName = "expenses";
                    query = "SELECT * FROM expenses WHERE id = ?";
                    break;
            }
            
            ResultSet rs = dbConnection.executeQuery(query, recordId);
            
            if (rs.next()) {
                amountField.setText(String.valueOf(rs.getDouble("amount")));
                descriptionArea.setText(rs.getString("description"));
                
                switch (recordType) {
                    case "investment":
                        investorNameField.setText(rs.getString("investor_name"));
                        roiField.setText(String.valueOf(rs.getDouble("roi_percentage")));
                        dateSpinner.setValue(rs.getDate("investment_date"));
                        break;
                    case "earning":
                        earningSourceCombo.setSelectedItem(rs.getString("source"));
                        dateSpinner.setValue(rs.getDate("month_year"));
                        break;
                    case "expense":
                        expenseCategoryCombo.setSelectedItem(rs.getString("category"));
                        dateSpinner.setValue(rs.getDate("month_year"));
                        break;
                }
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading record data: " + e.getMessage());
        }
    }
    
    private void saveRecord() {
        // Validate common fields
        if (amountField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Amount is required.");
            amountField.requestFocus();
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be a positive number.");
                amountField.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.");
            amountField.requestFocus();
            return;
        }
        
        if (dateSpinner.getValue() == null) {
            JOptionPane.showMessageDialog(this, "Date is required.");
            return;
        }
        
        // Type-specific validation
        if ("investment".equals(recordType)) {
            if (investorNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Investor name is required.");
                investorNameField.requestFocus();
                return;
            }
            
            if (roiField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "ROI percentage is required.");
                roiField.requestFocus();
                return;
            }
            
            try {
                double roi = Double.parseDouble(roiField.getText().trim());
                if (roi < 0) {
                    JOptionPane.showMessageDialog(this, "ROI percentage cannot be negative.");
                    roiField.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid ROI percentage.");
                roiField.requestFocus();
                return;
            }
        }
        
        try {
            String description = descriptionArea.getText().trim();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = dateFormat.format((Date) dateSpinner.getValue());
            
            if (recordId == null) {
                // Insert new record
                switch (recordType) {
                    case "investment":
                        String investorName = investorNameField.getText().trim();
                        double roi = Double.parseDouble(roiField.getText().trim());
                        String insertInvestmentQuery = "INSERT INTO investments (investor_name, amount, roi_percentage, description, investment_date, investor_contact) " +
                                                     "VALUES (?, ?, ?, ?, ?, NULL)";
                        dbConnection.executeUpdate(insertInvestmentQuery, investorName, amount, roi, description, formattedDate);
                        break;
                    case "earning":
                        String source = (String) earningSourceCombo.getSelectedItem();
                        String insertEarningQuery = "INSERT INTO earnings (source, amount, description, month_year) " +
                                                  "VALUES (?, ?, ?, ?)";
                        dbConnection.executeUpdate(insertEarningQuery, source, amount, description, formattedDate);
                        break;
                    case "expense":
                        String category = (String) expenseCategoryCombo.getSelectedItem();
                        String insertExpenseQuery = "INSERT INTO expenses (category, amount, description, month_year) " +
                                                   "VALUES (?, ?, ?, ?)";
                        dbConnection.executeUpdate(insertExpenseQuery, category, amount, description, formattedDate);
                        break;
                }
                JOptionPane.showMessageDialog(this, recordType.substring(0, 1).toUpperCase() + 
                                            recordType.substring(1) + " added successfully!");
            } else {
                // Update existing record
                switch (recordType) {
                    case "investment":
                        String investorName = investorNameField.getText().trim();
                        double roi = Double.parseDouble(roiField.getText().trim());
                        String updateInvestmentQuery = "UPDATE investments SET investor_name = ?, amount = ?, roi_percentage = ?, " +
                                                   "description = ?, investment_date = ?, investor_contact = NULL WHERE id = ?";
                        dbConnection.executeUpdate(updateInvestmentQuery, investorName, amount, roi, description, formattedDate, recordId);
                        break;
                    case "earning":
                        String source = (String) earningSourceCombo.getSelectedItem();
                        String updateEarningQuery = "UPDATE earnings SET source = ?, amount = ?, description = ?, " +
                                                  "month_year = ? WHERE id = ?";
                        dbConnection.executeUpdate(updateEarningQuery, source, amount, description, formattedDate, recordId);
                        break;
                    case "expense":
                        String category = (String) expenseCategoryCombo.getSelectedItem();
                        String updateExpenseQuery = "UPDATE expenses SET category = ?, amount = ?, description = ?, " +
                                                   "month_year = ? WHERE id = ?";
                        dbConnection.executeUpdate(updateExpenseQuery, category, amount, description, formattedDate, recordId);
                        break;
                }
                JOptionPane.showMessageDialog(this, recordType.substring(0, 1).toUpperCase() + 
                                            recordType.substring(1) + " updated successfully!");
            }
            
            dataSaved = true;
            dispose();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving record: " + e.getMessage());
        }
    }
    
    public boolean isDataSaved() {
        return dataSaved;
    }
}