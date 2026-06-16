import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class MembersPanel extends JPanel {
    private DatabaseConnection dbConnection;
    private JTable membersTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    public MembersPanel(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
        initializeComponents();
        loadMembersData();
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
                searchMembers();
            }
        });
        topPanel.add(searchField);
        
        JButton addButton = new JButton("Add Member");
        addButton.addActionListener(e -> showAddMemberDialog());
        topPanel.add(addButton);
        
        JButton editButton = new JButton("Edit Member");
        editButton.addActionListener(e -> editSelectedMember());
        topPanel.add(editButton);
        
        JButton deleteButton = new JButton("Delete Member");
        deleteButton.addActionListener(e -> deleteSelectedMember());
        topPanel.add(deleteButton);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadMembersData());
        topPanel.add(refreshButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {"ID", "Full Name", "Email", "Phone", "Membership", 
                               "Fee Amount", "Fee Status", "Fines", "Trainer", "Join Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        membersTable = new JTable(tableModel);
        membersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        membersTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        membersTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        membersTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        membersTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        membersTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        membersTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        membersTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        membersTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        membersTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        membersTable.getColumnModel().getColumn(8).setPreferredWidth(120);
        membersTable.getColumnModel().getColumn(9).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(membersTable);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadMembersData() {
        try {
            tableModel.setRowCount(0);
            String query = "SELECT m.id, m.full_name, m.email, m.phone, mt.name as membership, " +
                          "m.fee_amount, m.fee_status, m.fines, e.full_name as trainer, m.join_date " +
                          "FROM members m " +
                          "LEFT JOIN membership_types mt ON m.membership_type_id = mt.id " +
                          "LEFT JOIN employees e ON m.trainer_id = e.id " +
                          "ORDER BY m.id";
            
            ResultSet rs = dbConnection.executeQuery(query);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("full_name"));
                row.add(rs.getString("email"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("membership"));
                row.add(rs.getDouble("fee_amount"));
                row.add(rs.getString("fee_status"));
                row.add(rs.getDouble("fines"));
                row.add(rs.getString("trainer"));
                row.add(rs.getDate("join_date"));
                tableModel.addRow(row);
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading members data: " + e.getMessage());
        }
    }
    
    private void searchMembers() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadMembersData();
            return;
        }
        
        try {
            tableModel.setRowCount(0);
            String query = "SELECT m.id, m.full_name, m.email, m.phone, mt.name as membership, " +
                          "m.fee_amount, m.fee_status, m.fines, e.full_name as trainer, m.join_date " +
                          "FROM members m " +
                          "LEFT JOIN membership_types mt ON m.membership_type_id = mt.id " +
                          "LEFT JOIN employees e ON m.trainer_id = e.id " +
                          "WHERE LOWER(m.full_name) LIKE ? OR LOWER(m.email) LIKE ? OR LOWER(m.phone) LIKE ? " +
                          "ORDER BY m.id";
            
            String searchPattern = "%" + searchText + "%";
            ResultSet rs = dbConnection.executeQuery(query, searchPattern, searchPattern, searchPattern);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("full_name"));
                row.add(rs.getString("email"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("membership"));
                row.add(rs.getDouble("fee_amount"));
                row.add(rs.getString("fee_status"));
                row.add(rs.getDouble("fines"));
                row.add(rs.getString("trainer"));
                row.add(rs.getDate("join_date"));
                tableModel.addRow(row);
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching members: " + e.getMessage());
        }
    }
    
    private void showAddMemberDialog() {
        MemberDialog dialog = new MemberDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                              dbConnection, null);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadMembersData();
        }
    }
    
    private void editSelectedMember() {
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a member to edit.");
            return;
        }
        
        int memberId = (Integer) tableModel.getValueAt(selectedRow, 0);
        MemberDialog dialog = new MemberDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                              dbConnection, memberId);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadMembersData();
        }
    }
    
    private void deleteSelectedMember() {
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a member to delete.");
            return;
        }
        
        int memberId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String memberName = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete member: " + memberName + "?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dbConnection.executeUpdate("DELETE FROM members WHERE id = ?", memberId);
                JOptionPane.showMessageDialog(this, "Member deleted successfully!");
                loadMembersData();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting member: " + e.getMessage());
            }
        }
    }
}