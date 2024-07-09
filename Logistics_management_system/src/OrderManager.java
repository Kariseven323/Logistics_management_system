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
            System.out.println();
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