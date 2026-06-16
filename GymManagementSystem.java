import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class GymManagementSystem extends JFrame {
    private DatabaseConnection dbConnection;
    private JTabbedPane tabbedPane;
    public GymManagementSystem() {
        dbConnection = new DatabaseConnection();
        showLoginDialog();
    }
    
    private void showLoginDialog() {
        JDialog loginDialog = new JDialog(this, "Admin Login", true);
        loginDialog.setSize(450, 600);
        loginDialog.setLocationRelativeTo(null);
        loginDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        loginDialog.setResizable(true);
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                Color color1 = new Color(41, 128, 185);
                Color color2 = new Color(44, 62, 80);
                GradientPaint gp = new GradientPaint(0, 0, color1, w, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(new BorderLayout());
        
        // Login panel
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBackground(new Color(255, 255, 255, 240));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        // Add some padding at the top
        loginPanel.add(Box.createVerticalStrut(20));
        
        // Title
        JLabel titleLabel = new JLabel("Gym Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.add(titleLabel);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Admin Login");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.add(Box.createVerticalStrut(10));
        loginPanel.add(subtitleLabel);
        
        loginPanel.add(Box.createVerticalStrut(50));
        
        // Username field
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLabel.setForeground(new Color(44, 62, 80));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginPanel.add(userLabel);
        loginPanel.add(Box.createVerticalStrut(10));
        
        JTextField usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(350, 40));
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        loginPanel.add(usernameField);
        
        loginPanel.add(Box.createVerticalStrut(20));
        
        // Password field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        passLabel.setForeground(new Color(44, 62, 80));
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginPanel.add(passLabel);
        loginPanel.add(Box.createVerticalStrut(10));
        
        JPasswordField passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(350, 40));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        loginPanel.add(passwordField);
        
        loginPanel.add(Box.createVerticalStrut(40));
        
        // Login button
        JButton loginButton = new JButton("LOGIN");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(new Color(41, 128, 185));
        loginButton.setMaximumSize(new Dimension(350, 45));
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add hover effect
        loginButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(52, 152, 219));
            }
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(41, 128, 185));
            }
        });
        
        loginPanel.add(loginButton);
        
        // Center the login panel in the main panel
        JPanel centeringPanel = new JPanel(new GridBagLayout());
        centeringPanel.setOpaque(false);
        centeringPanel.add(loginPanel);
        mainPanel.add(centeringPanel, BorderLayout.CENTER);
        
        loginButton.addActionListener(e -> {
              String username = usernameField.getText();
              String password = new String(passwordField.getPassword());
              
              if (authenticateUser(username, password)) {
                  loginDialog.dispose();
                  initializeMainWindow();
              } else {
                  JOptionPane.showMessageDialog(loginDialog,
                      "Invalid username or password!",
                      "Login Error",
                      JOptionPane.ERROR_MESSAGE);
                  passwordField.setText("");
              }
          });
          
          loginDialog.getContentPane().add(mainPanel);
          
          // Enter key support
          ActionListener loginAction = e -> loginButton.doClick();
          usernameField.addActionListener(loginAction);
          passwordField.addActionListener(loginAction);
          
          loginDialog.setVisible(true);
    }
    
    private boolean authenticateUser(String username, String password) {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT * FROM admin WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            boolean authenticated = rs.next();
            
            rs.close();
            pstmt.close();
            
            return authenticated;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Database connection error: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void initializeMainWindow() {
        setTitle("Gym Management System - Admin Panel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);
        
        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> 
            JOptionPane.showMessageDialog(this, 
                "Gym Management System v1.0\nDeveloped for OOP Project\nCreated by: Affan, Adnan, Tauheed, Nandlal, Ishaque", 
                "About", 
                JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Add tabs
        tabbedPane.addTab("Members", new MembersPanel(dbConnection));
        tabbedPane.addTab("Employees", new EmployeesPanel(dbConnection));
        tabbedPane.addTab("Memberships", new MembershipsPanel(dbConnection));
        tabbedPane.addTab("Products", new ProductsPanel(dbConnection));
        tabbedPane.addTab("Equipment", new EquipmentPanel(dbConnection));
        tabbedPane.addTab("Financials", new FinancialsPanel(dbConnection));
        
        add(tabbedPane);
        setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new GymManagementSystem();
        });
    }
}