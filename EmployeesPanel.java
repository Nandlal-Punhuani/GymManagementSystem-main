import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class EmployeesPanel extends JPanel {
    private DatabaseConnection dbConnection;
    private JTable employeesTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    public EmployeesPanel(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
        initializeComponents();
        loadEmployeesData();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Top panel with search and buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        topPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchEmployees();
            }
        });
        topPanel.add(searchField);
        
        JButton addButton = new JButton("Add Employee");
        addButton.addActionListener(e -> showAddEmployeeDialog());
        topPanel.add(addButton);
        
        JButton editButton = new JButton("Edit Employee");
        editButton.addActionListener(e -> editSelectedEmployee());
        topPanel.add(editButton);
        
        JButton deleteButton = new JButton("Delete Employee");
        deleteButton.addActionListener(e -> deleteSelectedEmployee());
        topPanel.add(deleteButton);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadEmployeesData());
        topPanel.add(refreshButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {"ID", "Full Name", "Email", "Phone", "Occupation", 
                               "Salary", "Bonus", "Working Hours", "Days Off", "Trainer Available"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        employeesTable = new JTable(tableModel);
        employeesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeesTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        employeesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        employeesTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        employeesTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        employeesTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        employeesTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        employeesTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        employeesTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        employeesTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        employeesTable.getColumnModel().getColumn(8).setPreferredWidth(100);
        employeesTable.getColumnModel().getColumn(9).setPreferredWidth(120);
        
        JScrollPane scrollPane = new JScrollPane(employeesTable);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadEmployeesData() {
        try {
            tableModel.setRowCount(0);
            String query = "SELECT * FROM employees ORDER BY id";
            
            ResultSet rs = dbConnection.executeQuery(query);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("full_name"));
                row.add(rs.getString("email"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("occupation"));
                row.add(rs.getDouble("salary"));
                row.add(rs.getDouble("bonus"));
                row.add(rs.getInt("working_hours"));
                row.add(rs.getString("days_off"));
                row.add(rs.getBoolean("trainer_available") ? "Yes" : "No");
                tableModel.addRow(row);
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading employees data: " + e.getMessage());
        }
    }
    
    private void searchEmployees() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadEmployeesData();
            return;
        }
        
        try {
            tableModel.setRowCount(0);
            String query = "SELECT * FROM employees " +
                          "WHERE LOWER(full_name) LIKE ? OR LOWER(email) LIKE ? OR LOWER(occupation) LIKE ? " +
                          "ORDER BY id";
            
            String searchPattern = "%" + searchText + "%";
            ResultSet rs = dbConnection.executeQuery(query, searchPattern, searchPattern, searchPattern);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("full_name"));
                row.add(rs.getString("email"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("occupation"));
                row.add(rs.getDouble("salary"));
                row.add(rs.getDouble("bonus"));
                row.add(rs.getInt("working_hours"));
                row.add(rs.getString("days_off"));
                row.add(rs.getBoolean("trainer_available") ? "Yes" : "No");
                tableModel.addRow(row);
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching employees: " + e.getMessage());
        }
    }
    
    private void showAddEmployeeDialog() {
        EmployeeDialog dialog = new EmployeeDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                  dbConnection, null);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadEmployeesData();
        }
    }
    
    private void editSelectedEmployee() {
        int selectedRow = employeesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to edit.");
            return;
        }
        
        int employeeId = (Integer) tableModel.getValueAt(selectedRow, 0);
        EmployeeDialog dialog = new EmployeeDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                  dbConnection, employeeId);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadEmployeesData();
        }
    }
    
    private void deleteSelectedEmployee() {
        int selectedRow = employeesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete.");
            return;
        }
        
        int employeeId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String employeeName = (String) tableModel.getValueAt(selectedRow, 1);
        
        // Check if employee is assigned to any members
        try {
            ResultSet rs = dbConnection.executeQuery("SELECT COUNT(*) as count FROM members WHERE trainer_id = ?", employeeId);
            if (rs.next() && rs.getInt("count") > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Cannot delete employee. This employee is assigned as a trainer to some members.", 
                    "Cannot Delete", 
                    JOptionPane.WARNING_MESSAGE);
                rs.close();
                return;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error checking employee assignments: " + e.getMessage());
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete employee: " + employeeName + "?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dbConnection.executeUpdate("DELETE FROM employees WHERE id = ?", employeeId);
                JOptionPane.showMessageDialog(this, "Employee deleted successfully!");
                loadEmployeesData();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting employee: " + e.getMessage());
            }
        }
    }
}