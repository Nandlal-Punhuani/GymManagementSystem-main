import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class ProductsPanel extends JPanel {
    private DatabaseConnection dbConnection;
    private JTable productsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    public ProductsPanel(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
        initializeComponents();
        loadProductsData();
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
                searchProducts();
            }
        });
        topPanel.add(searchField);
        
        JButton addButton = new JButton("Add Product");
        addButton.addActionListener(e -> showAddProductDialog());
        topPanel.add(addButton);
        
        JButton editButton = new JButton("Edit Product");
        editButton.addActionListener(e -> editSelectedProduct());
        topPanel.add(editButton);
        
        JButton deleteButton = new JButton("Delete Product");
        deleteButton.addActionListener(e -> deleteSelectedProduct());
        topPanel.add(deleteButton);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadProductsData());
        topPanel.add(refreshButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {"ID", "Product Code", "Name", "Cost Price", 
                               "Selling Price", "Quantity", "Profit Margin"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        productsTable = new JTable(tableModel);
        productsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productsTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        productsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        productsTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        productsTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        productsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        productsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        productsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        productsTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(productsTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel with summary
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel summaryLabel = new JLabel("Total Products: 0 | Total Inventory Value: $0.00");
        bottomPanel.add(summaryLabel);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Update summary when table changes
        tableModel.addTableModelListener(e -> updateSummary(summaryLabel));
    }
    
    private void loadProductsData() {
        try {
            tableModel.setRowCount(0);
            String query = "SELECT * FROM products ORDER BY id";
            
            ResultSet rs = dbConnection.executeQuery(query);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("product_code"));
                row.add(rs.getString("name"));
                
                double costPrice = rs.getDouble("cost_price");
                double sellingPrice = rs.getDouble("selling_price");
                
                row.add(costPrice);
                row.add(sellingPrice);
                row.add(rs.getInt("quantity"));
                
                // Calculate profit margin
                double profitMargin = costPrice > 0 ? ((sellingPrice - costPrice) / costPrice) * 100 : 0;
                row.add(String.format("%.1f%%", profitMargin));
                
                tableModel.addRow(row);
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products data: " + e.getMessage());
        }
    }
    
    private void searchProducts() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadProductsData();
            return;
        }
        
        try {
            tableModel.setRowCount(0);
            String query = "SELECT * FROM products " +
                          "WHERE LOWER(name) LIKE ? OR LOWER(product_code) LIKE ? " +
                          "ORDER BY id";
            
            String searchPattern = "%" + searchText + "%";
            ResultSet rs = dbConnection.executeQuery(query, searchPattern, searchPattern);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("product_code"));
                row.add(rs.getString("name"));
                
                double costPrice = rs.getDouble("cost_price");
                double sellingPrice = rs.getDouble("selling_price");
                
                row.add(costPrice);
                row.add(sellingPrice);
                row.add(rs.getInt("quantity"));
                
                // Calculate profit margin
                double profitMargin = costPrice > 0 ? ((sellingPrice - costPrice) / costPrice) * 100 : 0;
                row.add(String.format("%.1f%%", profitMargin));
                
                tableModel.addRow(row);
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching products: " + e.getMessage());
        }
    }
    
    private void updateSummary(JLabel summaryLabel) {
        int totalProducts = tableModel.getRowCount();
        double totalValue = 0.0;
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            double costPrice = (Double) tableModel.getValueAt(i, 3);
            int quantity = (Integer) tableModel.getValueAt(i, 5);
            totalValue += costPrice * quantity;
        }
        
        summaryLabel.setText(String.format("Total Products: %d | Total Inventory Value: $%.2f", 
                                          totalProducts, totalValue));
    }
    
    private void showAddProductDialog() {
        ProductDialog dialog = new ProductDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                 dbConnection, null);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadProductsData();
        }
    }
    
    private void editSelectedProduct() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit.");
            return;
        }
        
        int productId = (Integer) tableModel.getValueAt(selectedRow, 0);
        ProductDialog dialog = new ProductDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                 dbConnection, productId);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadProductsData();
        }
    }
    
    private void deleteSelectedProduct() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.");
            return;
        }
        
        int productId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String productName = (String) tableModel.getValueAt(selectedRow, 2);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete product: " + productName + "?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dbConnection.executeUpdate("DELETE FROM products WHERE id = ?", productId);
                JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                loadProductsData();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting product: " + e.getMessage());
            }
        }
    }
}