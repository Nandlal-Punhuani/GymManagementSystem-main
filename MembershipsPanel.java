import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class MembershipsPanel extends JPanel {
    private DatabaseConnection dbConnection;
    private JTable membershipsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    public MembershipsPanel(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
        initializeComponents();
        loadMembershipsData();
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
                searchMemberships();
            }
        });
        topPanel.add(searchField);
        
        JButton addButton = new JButton("Add Membership");
        addButton.addActionListener(e -> showAddMembershipDialog());
        topPanel.add(addButton);
        
        JButton editButton = new JButton("Edit Membership");
        editButton.addActionListener(e -> editSelectedMembership());
        topPanel.add(editButton);
        
        JButton deleteButton = new JButton("Delete Membership");
        deleteButton.addActionListener(e -> deleteSelectedMembership());
        topPanel.add(deleteButton);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadMembershipsData());
        topPanel.add(refreshButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {"ID", "Name", "Payment Plan", "Monthly Fee", 
                               "Equipment Access", "Trainer Included"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        membershipsTable = new JTable(tableModel);
        membershipsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        membershipsTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        membershipsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        membershipsTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        membershipsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        membershipsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        membershipsTable.getColumnModel().getColumn(4).setPreferredWidth(300);
        membershipsTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        
        JScrollPane scrollPane = new JScrollPane(membershipsTable);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadMembershipsData() {
        try {
            tableModel.setRowCount(0);
            String query = "SELECT * FROM membership_types ORDER BY id";
            
            ResultSet rs = dbConnection.executeQuery(query);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("payment_plan"));
                row.add(rs.getDouble("monthly_fee"));
                row.add(rs.getString("equipment_access"));
                row.add(rs.getBoolean("trainer_included") ? "Yes" : "No");
                tableModel.addRow(row);
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading memberships data: " + e.getMessage());
        }
    }
    
    private void searchMemberships() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadMembershipsData();
            return;
        }
        
        try {
            tableModel.setRowCount(0);
            String query = "SELECT * FROM membership_types " +
                          "WHERE LOWER(name) LIKE ? OR LOWER(payment_plan) LIKE ? " +
                          "ORDER BY id";
            
            String searchPattern = "%" + searchText + "%";
            ResultSet rs = dbConnection.executeQuery(query, searchPattern, searchPattern);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("payment_plan"));
                row.add(rs.getDouble("monthly_fee"));
                row.add(rs.getString("equipment_access"));
                row.add(rs.getBoolean("trainer_included") ? "Yes" : "No");
                tableModel.addRow(row);
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching memberships: " + e.getMessage());
        }
    }
    
    private void showAddMembershipDialog() {
        MembershipDialog dialog = new MembershipDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                       dbConnection, null);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadMembershipsData();
        }
    }
    
    private void editSelectedMembership() {
        int selectedRow = membershipsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a membership to edit.");
            return;
        }
        
        int membershipId = (Integer) tableModel.getValueAt(selectedRow, 0);
        MembershipDialog dialog = new MembershipDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                       dbConnection, membershipId);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadMembershipsData();
        }
    }
    
    private void deleteSelectedMembership() {
        int selectedRow = membershipsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a membership to delete.");
            return;
        }
        
        int membershipId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String membershipName = (String) tableModel.getValueAt(selectedRow, 1);
        
        // Check if membership is assigned to any members
        try {
            ResultSet rs = dbConnection.executeQuery("SELECT COUNT(*) as count FROM members WHERE membership_type_id = ?", membershipId);
            if (rs.next() && rs.getInt("count") > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Cannot delete membership. This membership type is assigned to some members.", 
                    "Cannot Delete", 
                    JOptionPane.WARNING_MESSAGE);
                rs.close();
                return;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error checking membership assignments: " + e.getMessage());
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete membership: " + membershipName + "?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dbConnection.executeUpdate("DELETE FROM membership_types WHERE id = ?", membershipId);
                JOptionPane.showMessageDialog(this, "Membership deleted successfully!");
                loadMembershipsData();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting membership: " + e.getMessage());
            }
        }
    }
}