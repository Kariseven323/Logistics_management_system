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

    // 创建一个新司机
    public boolean addDriver(String driverID, String name, String phone, String licenseNumber) {
        String sql = "INSERT INTO Driver (DriverID, Name, Phone, LicenseNumber) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, driverID);
            pstmt.setString(2, name);
            pstmt.setString(3, phone);
            pstmt.setString(4, licenseNumber);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 创建一个新车辆
    public boolean addVehicle(String vehicleID, String licensePlate, String type, String status) {
        String sql = "INSERT INTO Vehicle (VehicleID, LicensePlate, Type, Status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, vehicleID);
            pstmt.setString(2, licensePlate);
            pstmt.setString(3, type);
            pstmt.setString(4, status);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 创建一个新货物
    public boolean addItem(String itemID, String name, double weight, double dimensions, double price) {
        String sql = "INSERT INTO Item (ItemID, Name, Weight, Dimensions, Price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, itemID);
            pstmt.setString(2, name);
            pstmt.setDouble(3, weight);
            pstmt.setDouble(4, dimensions);
            pstmt.setDouble(5, price);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 创建一个新仓库
    public boolean addWarehouse(String warehouseID, String name, String address, int capacity) {
        String sql = "INSERT INTO Warehouse (WarehouseID, Name, Address, Capacity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warehouseID);
            pstmt.setString(2, name);
            pstmt.setString(3, address);
            pstmt.setInt(4, capacity);
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

    public ResultSet getDriver(String driverID) throws SQLException {
        String query = "SELECT * FROM Driver WHERE DriverID = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, driverID);
        return preparedStatement.executeQuery();
    }

    public ResultSet getItem(String itemID) throws SQLException {
        String query = "SELECT * FROM Item WHERE ItemID = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, itemID);
        return preparedStatement.executeQuery();
    }


    public ResultSet getOrder(String orderID) throws SQLException {
        String query = "SELECT * FROM `Order` WHERE OrderID = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, orderID);
        return preparedStatement.executeQuery();
    }

    public ResultSet getShipment(String shipmentID) throws SQLException {
        String query = "SELECT * FROM Shipment WHERE ShipmentID = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, shipmentID);
        return preparedStatement.executeQuery();
    }

    public ResultSet getVehicle(String vehicleID) throws SQLException {
        String query = "SELECT * FROM Vehicle WHERE VehicleID = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, vehicleID);
        return preparedStatement.executeQuery();
    }

    public ResultSet getWarehouse(String warehouseID) throws SQLException {
        String query = "SELECT * FROM Warehouse WHERE WarehouseID = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, warehouseID);
        return preparedStatement.executeQuery();
    }

    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();

        // 添加一个新客户
        if (dbManager.addCustomer("0000000001", "John Doe", "1234 Elm St", "555-1234")) {
            System.out.println("客户添加成功！");
        } else {
            System.out.println("客户添加失败！");
        }

        // 创建一个新司机
        if (dbManager.addDriver("0000000001", "John Doe", "555-1234", "A123456")) {
            System.out.println("司机添加成功！");
        } else {
            System.out.println("司机添加失败！");
        }

        // 创建一个新车辆
        if (dbManager.addVehicle("0000000001", "ABC123", "Truck", "Available")) {
            System.out.println("车辆添加成功！");
        } else {
            System.out.println("车辆添加失败！");
        }

        // 创建一个新货物
        if (dbManager.addItem("0000000001", "Laptop", 2.5, 15.6, 999.99)) {
            System.out.println("货物添加成功！");
        } else {
            System.out.println("货物添加失败！");
        }

        // 创建一个新仓库
        if (dbManager.addWarehouse("0000000001", "Main Warehouse", "1234 Elm St", 10000)) {
            System.out.println("仓库添加成功！");
        } else {
            System.out.println("仓库添加失败！");
        }

        // 根据客户ID显示客户信息
        try (ResultSet rs = dbManager.getCustomer("0000000001")) {
            if (rs != null && rs.next()) {
                System.out.println("Customer ID: " + rs.getString("CustomerID"));
                System.out.println("Name: " + rs.getString("Name"));
                System.out.println("Address: " + rs.getString("Address"));
                System.out.println("Phone: " + rs.getString("Phone"));
            } else {
                System.out.println("客户没有找到");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 获取司机信息
        try (ResultSet rs = dbManager.getDriver("0000000001")) {
            if (rs != null && rs.next()) {
                System.out.println("Driver ID: " + rs.getString("DriverID"));
                System.out.println("Name: " + rs.getString("Name"));
                System.out.println("Phone: " + rs.getString("Phone"));
                System.out.println("License Number: " + rs.getString("LicenseNumber"));
            } else {
                System.out.println("司机没有找到");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 获取商品信息
        try (ResultSet rs = dbManager.getItem("0000000001")) {
            if (rs != null && rs.next()) {
                System.out.println("Item ID: " + rs.getString("ItemID"));
                System.out.println("Name: " + rs.getString("Name"));
                System.out.println("Weight: " + rs.getBigDecimal("Weight"));
                System.out.println("Dimensions: " + rs.getBigDecimal("Dimensions"));
                System.out.println("Price: " + rs.getBigDecimal("Price"));
            } else {
                System.out.println("商品没有找到");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 获取订单信息
        try (ResultSet rs = dbManager.getOrder("0000000001")) {
            if (rs != null && rs.next()) {
                System.out.println("Order ID: " + rs.getString("OrderID"));
                System.out.println("Customer ID: " + rs.getString("CustomerID"));
                System.out.println("Order Date: " + rs.getString("OrderDate"));
                System.out.println("Status: " + rs.getString("Status"));
            } else {
                System.out.println("订单没有找到");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 获取运输信息
        try (ResultSet rs = dbManager.getShipment("0000000001")) {
            if (rs != null && rs.next()) {
                System.out.println("Shipment ID: " + rs.getString("ShipmentID"));
                System.out.println("Order ID: " + rs.getString("OrderID"));
                System.out.println("Driver ID: " + rs.getString("DriverID"));
                System.out.println("Vehicle ID: " + rs.getString("VehicleID"));
                System.out.println("Shipment Date: " + rs.getString("ShipmentDate"));
                System.out.println("Shipment Status: " + rs.getString("ShipmentStatus"));
            } else {
                System.out.println("运输没有找到");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 获取车辆信息
        try (ResultSet rs = dbManager.getVehicle("0000000001")) {
            if (rs != null && rs.next()) {
                System.out.println("Vehicle ID: " + rs.getString("VehicleID"));
                System.out.println("License Plate: " + rs.getString("LicensePlate"));
                System.out.println("Type: " + rs.getString("Type"));
                System.out.println("Status: " + rs.getString("Status"));
            } else {
                System.out.println("车辆没有找到");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 获取仓库信息
        try (ResultSet rs = dbManager.getWarehouse("0000000001")) {
            if (rs != null && rs.next()) {
                System.out.println("Warehouse ID: " + rs.getString("WarehouseID"));
                System.out.println("Name: " + rs.getString("Name"));
                System.out.println("Address: " + rs.getString("Address"));
                System.out.println("Capacity: " + rs.getInt("Capacity"));
            } else {
                System.out.println("仓库没有找到");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        dbManager.close();
    }
}
