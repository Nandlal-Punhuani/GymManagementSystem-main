import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class EquipmentPanel extends JPanel {
    private DatabaseConnection dbConnection;
    private JTable equipmentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    public EquipmentPanel(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
        initializeComponents();
        loadEquipmentData();
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
                searchEquipment();
            }
        });
        topPanel.add(searchField);
        
        JButton addButton = new JButton("Add Equipment");
        addButton.addActionListener(e -> showAddEquipmentDialog());
        topPanel.add(addButton);
        
        JButton editButton = new JButton("Edit Equipment");
        editButton.addActionListener(e -> editSelectedEquipment());
        topPanel.add(editButton);
        
        JButton deleteButton = new JButton("Delete Equipment");
        deleteButton.addActionListener(e -> deleteSelectedEquipment());
        topPanel.add(deleteButton);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadEquipmentData());
        topPanel.add(refreshButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {"ID", "Equipment ID", "Name", "Worth", 
                               "Condition", "Action Required"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        equipmentTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                
                // Color coding based on condition
                if (!isRowSelected(row)) {
                    String condition = (String) getModel().getValueAt(row, 4);
                    switch (condition) {
                        case "Excellent":
                            component.setBackground(new Color(200, 255, 200)); // Light green
                            break;
                        case "Good":
                            component.setBackground(new Color(255, 255, 200)); // Light yellow
                            break;
                        case "Poor":
                            component.setBackground(new Color(255, 220, 200)); // Light orange
                            break;
                        case "Damaged":
                            component.setBackground(new Color(255, 200, 200)); // Light red
                            break;
                        default:
                            component.setBackground(Color.WHITE);
                    }
                } else {
                    component.setBackground(getSelectionBackground());
                }
                
                return component;
            }
        };
        
        equipmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        equipmentTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        equipmentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        equipmentTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        equipmentTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        equipmentTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        equipmentTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        equipmentTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(equipmentTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel with summary and legend
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Summary
        JLabel summaryLabel = new JLabel("Total Equipment: 0 | Total Worth: $0.00");
        bottomPanel.add(summaryLabel, BorderLayout.WEST);
        
        // Legend
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        legendPanel.add(new JLabel("Legend:"));
        
        JLabel excellentLabel = new JLabel(" Excellent ");
        excellentLabel.setOpaque(true);
        excellentLabel.setBackground(new Color(200, 255, 200));
        legendPanel.add(excellentLabel);
        
        JLabel goodLabel = new JLabel(" Good ");
        goodLabel.setOpaque(true);
        goodLabel.setBackground(new Color(255, 255, 200));
        legendPanel.add(goodLabel);
        
        JLabel poorLabel = new JLabel(" Poor ");
        poorLabel.setOpaque(true);
        poorLabel.setBackground(new Color(255, 220, 200));
        legendPanel.add(poorLabel);
        
        JLabel damagedLabel = new JLabel(" Damaged ");
        damagedLabel.setOpaque(true);
        damagedLabel.setBackground(new Color(255, 200, 200));
        legendPanel.add(damagedLabel);
        
        bottomPanel.add(legendPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Update summary when table changes
        tableModel.addTableModelListener(e -> updateSummary(summaryLabel));
    }
    
    private void loadEquipmentData() {
        try {
            tableModel.setRowCount(0);
            String query = "SELECT * FROM gym_equipment ORDER BY id";
            
            ResultSet rs = dbConnection.executeQuery(query);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("equipment_id"));
                row.add(rs.getString("name"));
                row.add(rs.getDouble("worth"));
                row.add(rs.getString("condition_status"));
                row.add(rs.getString("maintenance_required"));
                tableModel.addRow(row);
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading equipment data: " + e.getMessage());
        }
    }
    
    private void searchEquipment() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadEquipmentData();
            return;
        }
        
        try {
            tableModel.setRowCount(0);
            String query = "SELECT * FROM gym_equipment " +
                          "WHERE LOWER(name) LIKE ? OR LOWER(equipment_id) LIKE ? OR LOWER(condition_status) LIKE ? " +
                          "ORDER BY id";
            
            String searchPattern = "%" + searchText + "%";
            ResultSet rs = dbConnection.executeQuery(query, searchPattern, searchPattern, searchPattern);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("equipment_id"));
                row.add(rs.getString("name"));
                row.add(rs.getDouble("worth"));
                row.add(rs.getString("condition_status"));
                row.add(rs.getString("maintenance_required"));
                tableModel.addRow(row);
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching equipment: " + e.getMessage());
        }
    }
    
    private void updateSummary(JLabel summaryLabel) {
        int totalEquipment = tableModel.getRowCount();
        double totalWorth = 0.0;
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            double worth = (Double) tableModel.getValueAt(i, 3);
            totalWorth += worth;
        }
        
        summaryLabel.setText(String.format("Total Equipment: %d | Total Worth: $%.2f", 
                                          totalEquipment, totalWorth));
    }
    
    private void showAddEquipmentDialog() {
        EquipmentDialog dialog = new EquipmentDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                    dbConnection, null);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadEquipmentData();
        }
    }
    
    private void editSelectedEquipment() {
        int selectedRow = equipmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select equipment to edit.");
            return;
        }
        
        int equipmentId = (Integer) tableModel.getValueAt(selectedRow, 0);
        EquipmentDialog dialog = new EquipmentDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                    dbConnection, equipmentId);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadEquipmentData();
        }
    }
    
    private void deleteSelectedEquipment() {
        int selectedRow = equipmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select equipment to delete.");
            return;
        }
        
        int equipmentId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String equipmentName = (String) tableModel.getValueAt(selectedRow, 2);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete equipment: " + equipmentName + "?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dbConnection.executeUpdate("DELETE FROM gym_equipment WHERE id = ?", equipmentId);
                JOptionPane.showMessageDialog(this, "Equipment deleted successfully!");
                loadEquipmentData();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting equipment: " + e.getMessage());
            }
        }
    }
}