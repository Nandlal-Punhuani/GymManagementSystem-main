# Gym Management System

A comprehensive GUI-based Gym Management Software built using Java Swing and MySQL database.

## Features

### 🔐 Security
- Password-protected admin access
- Secure database authentication

### 👥 Member Management
- Add, edit, and delete gym members
- Track membership packages and fees
- Monitor fee status (Paid/Overdue)
- Manage fines and trainer assignments
- Search and filter members

### 👨‍💼 Employee Management
- Manage employee information
- Track salaries, bonuses, and working hours
- Monitor days off and trainer availability
- Role-based employee categorization

### 🎫 Membership Types
- Create and manage different membership plans
- Set payment plans and monthly fees
- Configure equipment access permissions
- Include/exclude trainer services

### 🛍️ Product Management
- Inventory tracking with product codes
- Cost and selling price management
- Automatic profit margin calculations
- Stock quantity monitoring

### 🏋️ Equipment Management
- Equipment tracking with unique IDs
- Condition monitoring (Excellent/Good/Poor/Damaged)
- Automatic maintenance requirement detection
- Equipment worth and replacement tracking

### 💰 Financial Management
- Investment tracking with ROI calculations
- Earnings from multiple sources
- Expense categorization and tracking
- Automatic profit/loss calculations
- Comprehensive financial summaries

## Technology Stack

- **Frontend**: Java Swing
- **Backend**: Java
- **Database**: MySQL
- **Connectivity**: JDBC
- **Date Picker**: Java Swing JSpinner with DateModel

## Prerequisites

1. **Java Development Kit (JDK) 8 or higher**
2. **MySQL Server 5.7 or higher**
3. **MySQL JDBC Driver** (mysql-connector-java)

## Setup Instructions

### 1. Database Setup

1. Install MySQL Server on your system
2. Create a new database named `gym_management`
3. Import the database schema and sample data:
   ```sql
   mysql -u root -p gym_management < gym_database.sql
   ```

### 2. Java Dependencies

1. Download the required JAR files:
   - MySQL Connector/J: [Download](https://dev.mysql.com/downloads/connector/j/)

2. Add the JAR files to your project classpath

### 3. Database Configuration

Update the database connection settings in `DatabaseConnection.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/gym_management";
private static final String USERNAME = "root";
private static final String PASSWORD = "Abcd_123456789";
```

### 4. Compilation and Execution

1. Compile all Java files:
   ```bash
   javac -cp ".:mysql-connector-java-8.0.33.jar" *.java
   ```

2. Run the application:
   ```bash
   java -cp ".:mysql-connector-java-8.0.33.jar" GymManagementSystem
   ```

## Default Login Credentials

- **Username**: admin
- **Password**: admin123

## Project Structure

```
PROJECT FINAL/
├── gym_database.sql              # Database schema and sample data
├── GymManagementSystem.java      # Main application class
├── DatabaseConnection.java       # Database connectivity
├── MembersPanel.java            # Member management panel
├── MemberDialog.java            # Member add/edit dialog
├── EmployeesPanel.java          # Employee management panel
├── EmployeeDialog.java          # Employee add/edit dialog
├── MembershipsPanel.java        # Membership type management
├── MembershipDialog.java        # Membership add/edit dialog
├── ProductsPanel.java           # Product management panel
├── ProductDialog.java           # Product add/edit dialog
├── EquipmentPanel.java          # Equipment management panel
├── EquipmentDialog.java         # Equipment add/edit dialog
├── FinancialsPanel.java         # Financial management panel
├── FinancialDialog.java         # Financial record add/edit dialog
└── README.md                    # This file
```

## Database Schema

The system uses the following main tables:

- `admin` - Admin user credentials
- `membership_types` - Available membership plans
- `employees` - Employee information
- `members` - Gym member details
- `products` - Product inventory
- `gym_equipment` - Equipment tracking
- `investments` - Investment records
- `earnings` - Revenue tracking
- `expenses` - Expense management

## Usage Guide

### Getting Started
1. Launch the application
2. Login with admin credentials
3. Navigate through different tabs to manage various aspects

### Managing Members
1. Go to the "Members" tab
2. Use "Add Member" to register new members
3. Select a member and click "Edit" to modify details
4. Use the search box to find specific members

### Financial Tracking
1. Navigate to the "Financials" tab
2. Use sub-tabs for Investments, Earnings, and Expenses
3. View the Summary tab for profit/loss analysis

### Equipment Maintenance
1. Go to the "Equipment" tab
2. Equipment is color-coded by condition:
   - Green: Excellent condition
   - Yellow: Good condition
   - Orange: Poor condition (needs maintenance)
   - Red: Damaged (needs replacement)

## Features Highlights

- **Real-time Data**: All changes are immediately reflected in the database
- **Data Validation**: Comprehensive input validation and error handling
- **User-Friendly Interface**: Intuitive design with clear navigation
- **Search Functionality**: Quick search across all modules
- **Financial Analytics**: Automatic profit/loss calculations
- **Maintenance Alerts**: Equipment condition monitoring
- **Responsive Design**: Adapts to different screen sizes

## Troubleshooting

### Common Issues

1. **Database Connection Error**
   - Verify MySQL server is running
   - Check database credentials in `DatabaseConnection.java`
   - Ensure the database `gym_management` exists

2. **ClassNotFoundException**
   - Verify MySQL JDBC driver is in classpath
   - Verify all required dependencies are in classpath

3. **Login Failed**
   - Use default credentials: admin/admin123
   - Check if admin table has data

## Future Enhancements

- Member photo management
- Barcode/QR code integration
- Email notifications for due payments
- Backup and restore functionality
- Multi-language support
- Mobile app integration

## License

This project is developed for educational purposes as part of an Object-Oriented Programming course.

## Support

For any issues or questions, please refer to the course materials or contact the development team.