import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZSH
 */
public class OrderManager {
    private List<LogisticsOrder> orders = new ArrayList<>();

    public void addOrder(LogisticsOrder order) {
        orders.add(order);
    }

    public void updateOrder(String orderID, boolean delivered) {
        for (LogisticsOrder order : orders) {
            if (order.getOrderID().equals(orderID)) {
                order.setDelivered(delivered);
                break;
            }
        }
    }

    public void removeOrder(String orderID) {
        orders.removeIf(order -> order.getOrderID().equals(orderID));
    }

    public void printOrders() {
        for (LogisticsOrder order : orders) {
            System.out.println(order); // 确保 LogisticsOrder 类有合适的 toString() 方法
        }
    }

    public void saveOrdersToFile(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(orders);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadOrdersFromFile(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            orders = (List<LogisticsOrder>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public List<LogisticsOrder> getOrders() {
        return orders;
    }
}

// 确保 LogisticsOrder 类实现 Serializable 接口
class LogisticsOrder implements Serializable {
    private static final long serialVersionUID = 1L; // 建议添加这个字段
    private String orderID;
    private String customer;
    private String address;
    private java.util.Date orderDate;
    private double amount;
    private boolean delivered;

    // 构造方法、getter 和 setter 方法

    @Override
    public String toString() {
        return "LogisticsOrder{" +
                "orderID='" + orderID + '\'' +
                ", customer='" + customer + '\'' +
                ", address='" + address + '\'' +
                ", orderDate=" + orderDate +
                ", amount=" + amount +
                ", delivered=" + delivered +
                '}';
    }
}
