import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author ZSH
 */
public class OrderProcessor implements Runnable {
    private OrderManager orderManager;
    private String operation;
    private String orderID;
    private String clientName;
    private String  address;

    public OrderProcessor(OrderManager orderManager, String operation, String orderID, String clientName, String address) {
        this.orderManager = orderManager;
        this.operation = operation;
        this.orderID = orderID;
        this.clientName = clientName;
        this.address = address;
    }

    @Override
    public void run() {
        switch (operation) {
            case "add":
                BigDecimal amount = BigDecimal.valueOf(Math.random() * 1000).setScale(2, RoundingMode.HALF_UP);
                LogisticsOrder newOrder = new LogisticsOrder(orderID, clientName, address, new java.util.Date(), amount.doubleValue(), false);
                orderManager.addOrder(newOrder);
                break;
            case "update":
                orderManager.updateOrder(orderID, true);
                break;
            case "remove":
                orderManager.removeOrder(orderID);
                break;
            default:
                System.out.println("未知操作: " + operation);
        }
    }
}
