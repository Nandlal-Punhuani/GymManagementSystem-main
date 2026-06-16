import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ProductDialog extends JDialog {
    private DatabaseConnection dbConnection;
    private Integer productId; // null for new product
    private boolean dataSaved = false;
    
    private JTextField productCodeField, nameField, costPriceField, sellingPriceField, quantityField;
    private JLabel profitMarginLabel;
    
    public ProductDialog(JFrame parent, DatabaseConnection dbConnection, Integer productId) {
        super(parent, productId == null ? "Add New Product" : "Edit Product", true);
        this.dbConnection = dbConnection;
        this.productId = productId;
        
        initializeComponents();
        
        if (productId != null) {
            loadProductData();
        }
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Product Code
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(new JLabel("Product Code:"), gbc);
        
        productCodeField = new JTextField(20);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(productCodeField, gbc);
        
        // Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Product Name:"), gbc);
        
        nameField = new JTextField(20);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(nameField, gbc);
        
        // Cost Price
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Cost Price:"), gbc);
        
        costPriceField = new JTextField(20);
        costPriceField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateProfitMargin();
            }
        });
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(costPriceField, gbc);
        
        // Selling Price
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Selling Price:"), gbc);
        
        sellingPriceField = new JTextField(20);
        sellingPriceField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateProfitMargin();
            }
        });
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(sellingPriceField, gbc);
        
        // Profit Margin (calculated)
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Profit Margin:"), gbc);
        
        profitMarginLabel = new JLabel("0.0%");
        profitMarginLabel.setFont(profitMarginLabel.getFont().deriveFont(Font.BOLD));
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(profitMarginLabel, gbc);
        
        // Quantity
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Quantity:"), gbc);
        
        quantityField = new JTextField(20);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(quantityField, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveProduct());
        buttonPanel.add(saveButton);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void updateProfitMargin() {
        try {
            String costText = costPriceField.getText().trim();
            String sellingText = sellingPriceField.getText().trim();
            
            if (!costText.isEmpty() && !sellingText.isEmpty()) {
                double costPrice = Double.parseDouble(costText);
                double sellingPrice = Double.parseDouble(sellingText);
                
                if (costPrice > 0) {
                    double profitMargin = ((sellingPrice - costPrice) / costPrice) * 100;
                    profitMarginLabel.setText(String.format("%.1f%%", profitMargin));
                    
                    // Color coding for profit margin
                    if (profitMargin < 0) {
                        profitMarginLabel.setForeground(Color.RED);
                    } else if (profitMargin < 20) {
                        profitMarginLabel.setForeground(Color.ORANGE);
                    } else {
                        profitMarginLabel.setForeground(Color.GREEN);
                    }
                } else {
                    profitMarginLabel.setText("0.0%");
                    profitMarginLabel.setForeground(Color.BLACK);
                }
            } else {
                profitMarginLabel.setText("0.0%");
                profitMarginLabel.setForeground(Color.BLACK);
            }
        } catch (NumberFormatException e) {
            profitMarginLabel.setText("Invalid");
            profitMarginLabel.setForeground(Color.RED);
        }
    }
    
    private void loadProductData() {
        try {
            String query = "SELECT * FROM products WHERE id = ?";
            ResultSet rs = dbConnection.executeQuery(query, productId);
            
            if (rs.next()) {
                productCodeField.setText(rs.getString("product_code"));
                nameField.setText(rs.getString("name"));
                costPriceField.setText(String.valueOf(rs.getDouble("cost_price")));
                sellingPriceField.setText(String.valueOf(rs.getDouble("selling_price")));
                quantityField.setText(String.valueOf(rs.getInt("quantity")));
                updateProfitMargin();
            }
            
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading product data: " + e.getMessage());
        }
    }
    
    private void saveProduct() {
        try {
            // Validate input
            if (productCodeField.getText().trim().isEmpty() || 
                nameField.getText().trim().isEmpty() || 
                costPriceField.getText().trim().isEmpty() ||
                sellingPriceField.getText().trim().isEmpty() ||
                quantityField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
                return;
            }
            
            double costPrice = Double.parseDouble(costPriceField.getText());
            double sellingPrice = Double.parseDouble(sellingPriceField.getText());
            int quantity = Integer.parseInt(quantityField.getText());
            
            if (costPrice < 0 || sellingPrice < 0 || quantity < 0) {
                JOptionPane.showMessageDialog(this, "Prices and quantity must be non-negative.");
                return;
            }
            
            if (productId == null) {
                // Insert new product
                String query = "INSERT INTO products (product_code, name, cost_price, " +
                              "selling_price, quantity) VALUES (?, ?, ?, ?, ?)";
                
                dbConnection.executeUpdate(query, 
                    productCodeField.getText().trim(),
                    nameField.getText().trim(),
                    costPrice,
                    sellingPrice,
                    quantity);
                
                JOptionPane.showMessageDialog(this, "Product added successfully!");
            } else {
                // Update existing product
                String query = "UPDATE products SET product_code = ?, name = ?, " +
                              "cost_price = ?, selling_price = ?, quantity = ? WHERE id = ?";
                
                dbConnection.executeUpdate(query,
                    productCodeField.getText().trim(),
                    nameField.getText().trim(),
                    costPrice,
                    sellingPrice,
                    quantity,
                    productId);
                
                JOptionPane.showMessageDialog(this, "Product updated successfully!");
            }
            
            dataSaved = true;
            dispose();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for prices and quantity.");
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("Duplicate entry")) {
                JOptionPane.showMessageDialog(this, "Product code already exists. Please use a different code.");
            } else {
                JOptionPane.showMessageDialog(this, "Error saving product: " + e.getMessage());
            }
        }
    }
    
    public boolean isDataSaved() {
        return dataSaved;
    }
}