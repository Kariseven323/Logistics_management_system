import javax.swing.*; // 引入Java Swing库，用于创建图形用户界面
import java.awt.*; // 引入Java AWT库，用于图形界面布局
import java.awt.event.ActionEvent; // 引入Java AWT事件库，用于处理按钮点击事件
import java.awt.event.ActionListener; // 引入Java AWT事件监听器接口
import java.awt.event.WindowAdapter; // 引入Java AWT窗口适配器类
import java.awt.event.WindowEvent; // 引入Java AWT窗口事件类
import java.io.*; // 引入Java IO库，用于输入输出流操作
import java.net.Socket; // 引入Java网络库中的Socket类，用于创建客户端套接字
import java.util.Date; // 引入Java日期类

public class Client {
    private static final String SERVER_ADDRESS = "localhost"; // 服务器地址
    private static final int SERVER_PORT = 12345; // 服务器端口号

    private JFrame frame; // 主窗口
    private JTextField orderIDField; // 订单ID输入字段
    private JTextField senderField; // 客户姓名输入字段
    private JTextField receiverField; // 地址输入字段
    private JTextField itemField; // 金额输入字段
    private JCheckBox deliveredCheckBox; // 是否已交付复选框
    private JTextArea resultArea; // 显示结果的文本区域
    private JTextField messageField; // 消息输入字段
    private Socket socket; // 套接字
    private ObjectOutputStream oos; // 对象输出流
    private ObjectInputStream ois; // 对象输入流
    private JLabel portLabel; // 显示服务器端口的标签
    private JLabel localPortLabel; // 显示本地端口的标签

    public Client() {
        frame = new JFrame("Order Client"); // 创建主窗口
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 设置关闭操作
        frame.setSize(500, 400); // 设置窗口大小

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirmed = JOptionPane.showConfirmDialog(frame,
                        "Are you sure you want to exit the program?",
                        "Exit Program Message Box",
                        JOptionPane.YES_NO_OPTION);

                if (confirmed == JOptionPane.YES_OPTION) {
                    try {
                        if (socket != null) {
                            socket.close(); // 关闭套接字
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    frame.dispose(); // 关闭窗口
                    System.exit(0); // 退出程序
                }
            }
        });

        JPanel panel = new JPanel(); // 创建面板
        panel.setLayout(new GridLayout(0, 2, 10, 10)); // 设置网格布局

        panel.add(new JLabel("Order ID:")); // 添加订单ID标签
        orderIDField = new JTextField(); // 创建订单ID输入字段
        panel.add(orderIDField); // 添加订单ID输入字段到面板

        panel.add(new JLabel("Customer Name:")); // 添加客户姓名标签
        senderField = new JTextField(); // 创建客户姓名输入字段
        panel.add(senderField); // 添加客户姓名输入字段到面板

        panel.add(new JLabel("Address:")); // 添加地址标签
        receiverField = new JTextField(); // 创建地址输入字段
        panel.add(receiverField); // 添加地址输入字段到面板

        panel.add(new JLabel("Money:")); // 添加金额标签
        itemField = new JTextField(); // 创建金额输入字段
        panel.add(itemField); // 添加金额输入字段到面板

        panel.add(new JLabel("Delivered:")); // 添加是否已交付标签
        deliveredCheckBox = new JCheckBox(); // 创建是否已交付复选框
        panel.add(deliveredCheckBox); // 添加是否已交付复选框到面板

        JButton addButton = new JButton("Add Order"); // 创建添加订单按钮
        addButton.addActionListener(new AddButtonListener()); // 添加事件监听器
        panel.add(addButton); // 添加按钮到面板

        JButton updateButton = new JButton("Update Order"); // 创建更新订单按钮
        updateButton.addActionListener(new UpdateButtonListener()); // 添加事件监听器
        panel.add(updateButton); // 添加按钮到面板

        JButton removeButton = new JButton("Remove Order"); // 创建删除订单按钮
        removeButton.addActionListener(new RemoveButtonListener()); // 添加事件监听器
        panel.add(removeButton); // 添加按钮到面板

        JButton queryButton = new JButton("Query Order"); // 创建查询订单按钮
        queryButton.addActionListener(new QueryButtonListener()); // 添加事件监听器
        panel.add(queryButton); // 添加按钮到面板

        panel.add(new JLabel("Message:")); // 添加消息标签
        messageField = new JTextField(); // 创建消息输入字段
        panel.add(messageField); // 添加消息输入字段到面板

        JButton sendMessageButton = new JButton("Send Message"); // 创建发送消息按钮
        sendMessageButton.addActionListener(new SendMessageButtonListener()); // 添加事件监听器
        panel.add(sendMessageButton); // 添加按钮到面板

        portLabel = new JLabel("Connected to port: " + SERVER_PORT); // 显示当前端口
        panel.add(portLabel); // 添加标签到面板

        localPortLabel = new JLabel("Local port: N/A"); // 初始化本地端口标签
        panel.add(localPortLabel); // 添加标签到面板

        resultArea = new JTextArea(); // 创建结果显示区域
        resultArea.setEditable(false); // 设置为不可编辑
        resultArea.setLineWrap(true); // 设置自动换行
        resultArea.setWrapStyleWord(true); // 设置单词换行

        frame.getContentPane().add(panel, BorderLayout.NORTH); // 添加面板到窗口北部
        frame.getContentPane().add(new JScrollPane(resultArea), BorderLayout.CENTER); // 添加结果显示区域到窗口中心

        frame.setVisible(true); // 显示窗口

        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT); // 连接到服务器
            oos = new ObjectOutputStream(socket.getOutputStream()); // 初始化对象输出流
            ois = new ObjectInputStream(socket.getInputStream()); // 初始化对象输入流

            localPortLabel.setText("Local port: " + socket.getLocalPort()); // 显示本地端口

            new Thread(new ServerMessageListener()).start(); // 启动监听服务器消息的线程
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error connecting to server", "Error", JOptionPane.ERROR_MESSAGE); // 显示错误信息
        }
    }

    private class ServerMessageListener implements Runnable { // 监听服务器消息的线程类
        @Override
        public void run() {
            try {
                while (true) {
                    String command = (String) ois.readObject(); // 读取命令
                    if ("message".equals(command)) {
                        String message = (String) ois.readObject(); // 读取消息
                        resultArea.append("Server: " + message + "\n"); // 显示消息
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private class SendMessageButtonListener implements ActionListener { // 发送消息按钮的事件监听器
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = messageField.getText(); // 获取消息

            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Message field must be filled", "Error", JOptionPane.ERROR_MESSAGE); // 显示错误信息
                return;
            }

            try {
                oos.writeObject("message"); // 发送消息命令
                oos.writeObject(message); // 发送消息内容
                oos.flush(); // 刷新输出流
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error sending message to server", "Error", JOptionPane.ERROR_MESSAGE); // 显示错误信息
            }
        }
    }

    private class AddButtonListener implements ActionListener { // 添加订单按钮的事件监听器
        @Override
        public void actionPerformed(ActionEvent e) {
            String orderID = orderIDField.getText(); // 获取订单ID
            String customerName = senderField.getText(); // 获取客户姓名
            String address = receiverField.getText(); // 获取地址
            String money = itemField.getText(); // 获取金额
            boolean delivered = deliveredCheckBox.isSelected(); // 获取是否已交付

            if (orderID.isEmpty() || address.isEmpty() || customerName.isEmpty() || money.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields must be filled", "Error", JOptionPane.ERROR_MESSAGE); // 显示错误信息
                return;
            }

            double mon;
            try {
                mon = Double.parseDouble(money); // 转换金额为数字
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Item must be a valid number", "Error", JOptionPane.ERROR_MESSAGE); // 显示错误信息
                return;
            }

            Date orderDate = new Date(); // 获取当前日期
            LogisticsOrder newOrder = new LogisticsOrder(orderID, customerName, address, orderDate, mon, delivered); // 创建新订单

            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT); // 创建新的套接字
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); // 创建对象输出流
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) { // 创建对象输入流

                oos.writeObject("add"); // 发送添加命令
                oos.writeObject(newOrder); // 发送新订单

                String response = (String) ois.readObject(); // 读取服务器响应
                resultArea.setText(response); // 显示响应

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error connecting to server", "Error", JOptionPane.ERROR_MESSAGE); // 显示错误信息
            }
        }
    }

    private class UpdateButtonListener implements ActionListener { // 更新订单按钮的事件监听器
        @Override
        public void actionPerformed(ActionEvent e) {
            String orderID = orderIDField.getText(); // 获取订单ID
            boolean delivered = deliveredCheckBox.isSelected(); // 获取是否已交付

            if (orderID.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Order ID must be filled", "Error", JOptionPane.ERROR_MESSAGE); // 显示错误信息
                return;
            }

            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT); // 创建新的套接字
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); // 创建对象输出流
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) { // 创建对象输入流

                oos.writeObject("update"); // 发送更新命令
                oos.writeObject(orderID); // 发送订单ID
                oos.writeObject(delivered); // 发送是否已交付

                String response = (String) ois.readObject(); // 读取服务器响应
                resultArea.setText(response); // 显示响应

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error connecting to server", "Error", JOptionPane.ERROR_MESSAGE); // 显示错误信息
            }
        }
    }

    private class RemoveButtonListener implements ActionListener { // 删除订单按钮的事件监听器
        @Override
        public void actionPerformed(ActionEvent e) {
            String orderID = orderIDField.getText(); // 获取订单ID

            if (orderID.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Order ID must be filled", "Error", JOptionPane.ERROR_MESSAGE); // 显示错误信息
                return;
            }

            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT); // 创建新的套接字
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); // 创建对象输出流
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) { // 创建对象输入流

                oos.writeObject("remove"); // 发送删除命令
                oos.writeObject(orderID); // 发送订单ID

                String response = (String) ois.readObject(); // 读取服务器响应
                resultArea.setText(response); // 显示响应

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error connecting to server", "Error", JOptionPane.ERROR_MESSAGE); // 显示错误信息
            }
        }
    }

    private class QueryButtonListener implements ActionListener { // 查询订单按钮的事件监听器
        @Override
        public void actionPerformed(ActionEvent e) {
            String orderID = orderIDField.getText(); // 获取订单ID

            if (orderID.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Order ID must be filled", "Error", JOptionPane.ERROR_MESSAGE); // 显示错误信息
                return;
            }

            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT); // 创建新的套接字
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); // 创建对象输出流
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) { // 创建对象输入流

                oos.writeObject("query"); // 发送查询命令
                oos.writeObject(orderID); // 发送订单ID

                Object response = ois.readObject(); // 读取服务器响应
                if (response instanceof LogisticsOrder) {
                    LogisticsOrder order = (LogisticsOrder) response; // 读取订单信息
                    resultArea.setText(order.toString()); // 显示订单信息
                } else {
                    resultArea.setText((String) response); // 显示响应
                }

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error connecting to server", "Error", JOptionPane.ERROR_MESSAGE); // 显示错误信息
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new); // 启动客户端
    }
}
