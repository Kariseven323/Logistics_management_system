import java.io.Serializable;

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
        return "物流订单{" +
                "订单ID='" + orderID + '\'' +
                ", 客户='" + customer + '\'' +
                ", 地址='" + address + '\'' +
                ", 订单日期=" + orderDate +
                ", 金额=" + amount +
                ", 是否送达=" + delivered +
                '}';
    }
}
