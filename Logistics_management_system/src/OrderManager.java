import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZSH
 */
public class OrderManager {
    private List<LogisticsOrder> orders = new ArrayList<>();

    public void addOrder(LogisticsOrder order) {  //将一个新的 LogisticsOrder 对象添加到订单列表中
        orders.add(order);
    }

    public void updateOrder(String orderID, boolean delivered) {  //根据订单ID查找订单并更新其交付状态
        for (LogisticsOrder order : orders) {
            if (order.getOrderID().equals(orderID)) {
                order.setDelivered(delivered);
                System.out.println("订单已更新: " + order);
                break;
            }
        }
    }

    public void removeOrder(String orderID) {  //根据订单ID删除订单
        orders.removeIf(order -> order.getOrderID().equals(orderID));
        System.out.println("订单已删除: " + orderID);
    }

    public void printOrders() {  //打印所有订单
        System.out.println("当前订单列表:");
        for (LogisticsOrder order : orders) {
            System.out.println(order); // 确保 LogisticsOrder 类有合适的 toString() 方法
        }
    }

    public void saveOrdersToFile(String filename) {  //将所有订单保存到文件中
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(orders);
            System.out.println("订单已保存到文件: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadOrdersFromFile(String filename) {  //从文件中读取所有订单并反序列化
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            orders = (List<LogisticsOrder>) ois.readObject();
            System.out.println("订单已从文件加载: " + filename);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public List<LogisticsOrder> getOrders() {  //获取订单列表
        return orders;
    }

    public synchronized LogisticsOrder getOrderById(String orderID) {  //根据订单ID查找订单
        for (LogisticsOrder order : orders) {
            if (order.getOrderID().equals(orderID)) {
                return order;
            }
        }
        return null;
    }
}

