import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/logistics";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    private Connection connection;

    public DatabaseManager() {
        try {
            // 加载mysql
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 创建一个新客户
    public boolean addCustomer(String customerID, String name, String address, String phone) {
        String sql = "INSERT INTO Customer (CustomerID, Name, Address, Phone) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, customerID);
            pstmt.setString(2, name);
            pstmt.setString(3, address);
            pstmt.setString(4, phone);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 根据ID返回客户
    public ResultSet getCustomer(String customerID) {
        String sql = "SELECT * FROM Customer WHERE CustomerID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, customerID);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 创建一个新订单
    public boolean addOrder(String orderID, String customerID, String orderDate, String status) {
        String sql = "INSERT INTO `Order` (OrderID, CustomerID, OrderDate, Status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, orderID);
            pstmt.setString(2, customerID);
            pstmt.setString(3, orderDate);
            pstmt.setString(4, status);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();

        // Add a new customer
        if (dbManager.addCustomer("0000000001", "John Doe", "1234 Elm St", "555-1234")) {
            System.out.println("客户添加成功！");
        } else {
            System.out.println("客户添加失败！");
        }

        // Retrieve and display customer information
        try (ResultSet rs = dbManager.getCustomer("0000000001")) {
            if (rs != null && rs.next()) {
                System.out.println("Customer ID: " + rs.getString("CustomerID"));
                System.out.println("Name: " + rs.getString("Name"));
                System.out.println("Address: " + rs.getString("Address"));
                System.out.println("Phone: " + rs.getString("Phone"));
            } else {
                System.out.println("Customer not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        dbManager.close();
    }
}
