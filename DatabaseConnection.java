import java.sql.*;
import javax.swing.JOptionPane;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/gym_management";
    private static final String USERNAME = "root"; // Change as needed
    private static final String PASSWORD = "Abcd_123456789"; // Change as needed
    
    private Connection connection;
    
    public DatabaseConnection() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Database connected successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            JOptionPane.showMessageDialog(null, 
                "MySQL JDBC Driver not found!\nPlease add mysql-connector-java to your classpath.", 
                "Driver Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            JOptionPane.showMessageDialog(null, 
                "Database connection failed!\nPlease check if MySQL server is running and database exists.\n" +
                "Error: " + e.getMessage(), 
                "Connection Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Utility method to execute SELECT queries
    public ResultSet executeQuery(String query, Object... params) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        return pstmt.executeQuery();
    }
    
    // Utility method to execute INSERT, UPDATE, DELETE queries
    public int executeUpdate(String query, Object... params) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        int result = pstmt.executeUpdate();
        pstmt.close();
        return result;
    }
    
    // Utility method to get auto-generated keys
    public int executeUpdateWithGeneratedKeys(String query, Object... params) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        pstmt.executeUpdate();
        
        ResultSet rs = pstmt.getGeneratedKeys();
        int generatedId = -1;
        if (rs.next()) {
            generatedId = rs.getInt(1);
        }
        
        rs.close();
        pstmt.close();
        return generatedId;
    }
}