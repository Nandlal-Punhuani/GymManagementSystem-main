import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import java.text.DecimalFormat;

public class FinancialsPanel extends JPanel {
    private DatabaseConnection dbConnection;
    private JTabbedPane tabbedPane;
    private JTable investmentTable, earningTable, expenseTable;
    private DefaultTableModel investmentModel, earningModel, expenseModel;
    private JLabel profitLossLabel;
    private DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    
    public FinancialsPanel(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
        initializeComponents();
        loadAllData();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        tabbedPane = new JTabbedPane();
        
        // Investment Tab
        tabbedPane.addTab("Investments", createInvestmentPanel());
        
        // Earnings Tab
        tabbedPane.addTab("Earnings", createEarningsPanel());
        
        // Expenses Tab
        tabbedPane.addTab("Expenses", createExpensesPanel());
        
        // Summary Tab
        tabbedPane.addTab("Summary", createSummaryPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createInvestmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Top panel with buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addButton = new JButton("Add Investment");
        addButton.addActionListener(e -> showAddInvestmentDialog());
        topPanel.add(addButton);
        
        JButton editButton = new JButton("Edit Investment");
        editButton.addActionListener(e -> editSelectedInvestment());
        topPanel.add(editButton);
        
        JButton deleteButton = new JButton("Delete Investment");
        deleteButton.addActionListener(e -> deleteSelectedInvestment());
        topPanel.add(deleteButton);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadInvestmentData());
        topPanel.add(refreshButton);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {"ID", "Investor Name", "Amount", "ROI %", "Expected Return", "Description", "Date"};
        investmentModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        investmentTable = new JTable(investmentModel);
        investmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(investmentTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createEarningsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Top panel with buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addButton = new JButton("Add Earning");
        addButton.addActionListener(e -> showAddEarningDialog());
        topPanel.add(addButton);
        
        JButton editButton = new JButton("Edit Earning");
        editButton.addActionListener(e -> editSelectedEarning());
        topPanel.add(editButton);
        
        JButton deleteButton = new JButton("Delete Earning");
        deleteButton.addActionListener(e -> deleteSelectedEarning());
        topPanel.add(deleteButton);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadEarningData());
        topPanel.add(refreshButton);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {"ID", "Source", "Amount", "Description", "Date"};
        earningModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        earningTable = new JTable(earningModel);
        earningTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(earningTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createExpensesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Top panel with buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addButton = new JButton("Add Expense");
        addButton.addActionListener(e -> showAddExpenseDialog());
        topPanel.add(addButton);
        
        JButton editButton = new JButton("Edit Expense");
        editButton.addActionListener(e -> editSelectedExpense());
        topPanel.add(editButton);
        
        JButton deleteButton = new JButton("Delete Expense");
        deleteButton.addActionListener(e -> deleteSelectedExpense());
        topPanel.add(deleteButton);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadExpenseData());
        topPanel.add(refreshButton);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {"ID", "Category", "Amount", "Description", "Date"};
        expenseModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        expenseTable = new JTable(expenseModel);
        expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(expenseTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Summary information
        JPanel summaryPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Financial Summary");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        summaryPanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        
        // Investment summary
        gbc.gridx = 0; gbc.gridy = 1;
        summaryPanel.add(new JLabel("Total Investments:"), gbc);
        gbc.gridx = 1;
        JLabel totalInvestmentLabel = new JLabel("$0.00");
        summaryPanel.add(totalInvestmentLabel, gbc);
        
        // Earnings summary
        gbc.gridx = 0; gbc.gridy = 2;
        summaryPanel.add(new JLabel("Total Earnings:"), gbc);
        gbc.gridx = 1;
        JLabel totalEarningsLabel = new JLabel("$0.00");
        summaryPanel.add(totalEarningsLabel, gbc);
        
        // Expenses summary
        gbc.gridx = 0; gbc.gridy = 3;
        summaryPanel.add(new JLabel("Total Expenses:"), gbc);
        gbc.gridx = 1;
        JLabel totalExpensesLabel = new JLabel("$0.00");
        summaryPanel.add(totalExpensesLabel, gbc);
        
        // Net profit/loss
        gbc.gridx = 0; gbc.gridy = 4;
        summaryPanel.add(new JLabel("Net Profit/Loss:"), gbc);
        gbc.gridx = 1;
        profitLossLabel = new JLabel("$0.00");
        profitLossLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        summaryPanel.add(profitLossLabel, gbc);
        
        panel.add(summaryPanel, BorderLayout.NORTH);
        
        // Refresh button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshSummaryButton = new JButton("Refresh Summary");
        refreshSummaryButton.addActionListener(e -> updateSummary(totalInvestmentLabel, totalEarningsLabel, totalExpensesLabel));
        buttonPanel.add(refreshSummaryButton);
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        
        // Initialize summary
        updateSummary(totalInvestmentLabel, totalEarningsLabel, totalExpensesLabel);
        
        return panel;
    }
    
    private void loadAllData() {
        loadInvestmentData();
        loadEarningData();
        loadExpenseData();
    }
    
    private void loadInvestmentData() {
        try {
            investmentModel.setRowCount(0);
            String query = "SELECT * FROM investments ORDER BY investment_date DESC";
            
            ResultSet rs = dbConnection.executeQuery(query);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("investor_name"));
                row.add(currencyFormat.format(rs.getDouble("amount")));
                row.add(rs.getDouble("roi_percentage") + "%");
                
                double amount = rs.getDouble("amount");
                double roi = rs.getDouble("roi_percentage");
                double expectedReturn = amount * (1 + roi / 100);
                row.add(currencyFormat.format(expectedReturn));
                
                row.add(rs.getString("description"));
                row.add(rs.getDate("investment_date"));
                investmentModel.addRow(row);
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading investment data: " + e.getMessage());
        }
    }
    
    private void loadEarningData() {
        try {
            earningModel.setRowCount(0);
            String query = "SELECT * FROM earnings ORDER BY month_year DESC";
            
            ResultSet rs = dbConnection.executeQuery(query);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("source"));
                row.add(currencyFormat.format(rs.getDouble("amount")));
                row.add(rs.getString("description"));
                row.add(rs.getDate("month_year"));
                earningModel.addRow(row);
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading earning data: " + e.getMessage());
        }
    }
    
    private void loadExpenseData() {
        try {
            expenseModel.setRowCount(0);
            String query = "SELECT * FROM expenses ORDER BY month_year DESC";
            
            ResultSet rs = dbConnection.executeQuery(query);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("category"));
                row.add(currencyFormat.format(rs.getDouble("amount")));
                row.add(rs.getString("description"));
                row.add(rs.getDate("month_year"));
                expenseModel.addRow(row);
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading expense data: " + e.getMessage());
        }
    }
    
    private void updateSummary(JLabel totalInvestmentLabel, JLabel totalEarningsLabel, JLabel totalExpensesLabel) {
        try {
            // Calculate total investments
            double totalInvestments = 0;
            ResultSet rs = dbConnection.executeQuery("SELECT SUM(amount) FROM investments");
            if (rs.next()) {
                totalInvestments = rs.getDouble(1);
            }
            rs.close();
            
            // Calculate total earnings
            double totalEarnings = 0;
            rs = dbConnection.executeQuery("SELECT SUM(amount) FROM earnings");
            if (rs.next()) {
                totalEarnings = rs.getDouble(1);
            }
            rs.close();
            
            // Calculate total expenses
            double totalExpenses = 0;
            rs = dbConnection.executeQuery("SELECT SUM(amount) FROM expenses");
            if (rs.next()) {
                totalExpenses = rs.getDouble(1);
            }
            rs.close();
            
            // Calculate net profit/loss
            double netProfitLoss = (totalInvestments + totalEarnings) - totalExpenses;
            
            // Update labels
            totalInvestmentLabel.setText(currencyFormat.format(totalInvestments));
            totalEarningsLabel.setText(currencyFormat.format(totalEarnings));
            totalExpensesLabel.setText(currencyFormat.format(totalExpenses));
            
            profitLossLabel.setText(currencyFormat.format(netProfitLoss));
            if (netProfitLoss >= 0) {
                profitLossLabel.setForeground(new Color(0, 150, 0)); // Green for profit
            } else {
                profitLossLabel.setForeground(new Color(200, 0, 0)); // Red for loss
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error calculating summary: " + e.getMessage());
        }
    }
    
    // Investment methods
    private void showAddInvestmentDialog() {
        FinancialDialog dialog = new FinancialDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                    dbConnection, "investment", null);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadInvestmentData();
        }
    }
    
    private void editSelectedInvestment() {
        int selectedRow = investmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an investment to edit.");
            return;
        }
        
        int id = (Integer) investmentModel.getValueAt(selectedRow, 0);
        FinancialDialog dialog = new FinancialDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                    dbConnection, "investment", id);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadInvestmentData();
        }
    }
    
    private void deleteSelectedInvestment() {
        int selectedRow = investmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an investment to delete.");
            return;
        }
        
        int id = (Integer) investmentModel.getValueAt(selectedRow, 0);
        deleteFinancialRecord("investments", id, "investment");
        loadInvestmentData();
    }
    
    // Earning methods
    private void showAddEarningDialog() {
        FinancialDialog dialog = new FinancialDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                    dbConnection, "earning", null);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadEarningData();
        }
    }
    
    private void editSelectedEarning() {
        int selectedRow = earningTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an earning to edit.");
            return;
        }
        
        int id = (Integer) earningModel.getValueAt(selectedRow, 0);
        FinancialDialog dialog = new FinancialDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                    dbConnection, "earning", id);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadEarningData();
        }
    }
    
    private void deleteSelectedEarning() {
        int selectedRow = earningTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an earning to delete.");
            return;
        }
        
        int id = (Integer) earningModel.getValueAt(selectedRow, 0);
        deleteFinancialRecord("earnings", id, "earning");
        loadEarningData();
    }
    
    // Expense methods
    private void showAddExpenseDialog() {
        FinancialDialog dialog = new FinancialDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                    dbConnection, "expense", null);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadExpenseData();
        }
    }
    
    private void editSelectedExpense() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an expense to edit.");
            return;
        }
        
        int id = (Integer) expenseModel.getValueAt(selectedRow, 0);
        FinancialDialog dialog = new FinancialDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                    dbConnection, "expense", id);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadExpenseData();
        }
    }
    
    private void deleteSelectedExpense() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an expense to delete.");
            return;
        }
        
        int id = (Integer) expenseModel.getValueAt(selectedRow, 0);
        deleteFinancialRecord("expenses", id, "expense");
        loadExpenseData();
    }
    
    private void deleteFinancialRecord(String tableName, int id, String recordType) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this " + recordType + "?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dbConnection.executeUpdate("DELETE FROM " + tableName + " WHERE id = ?", id);
                JOptionPane.showMessageDialog(this, recordType.substring(0, 1).toUpperCase() + 
                                            recordType.substring(1) + " deleted successfully!");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting " + recordType + ": " + e.getMessage());
            }
        }
    }
}