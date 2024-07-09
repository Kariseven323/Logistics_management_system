import javax.swing.*; // 引入Java Swing库，用于创建图形用户界面
import java.awt.*; // 引入Java AWT库，用于图形界面布局
import java.awt.event.ActionEvent; // 引入Java AWT事件库，用于处理按钮点击事件
import java.awt.event.ActionListener; // 引入Java AWT事件监听器接口
import java.io.*; // 引入Java IO库，用于输入输出流操作
import java.net.ServerSocket; // 引入Java网络库中的ServerSocket类，用于创建服务器套接字
import java.net.Socket; // 引入Java网络库中的Socket类，用于创建客户端套接字
import java.util.ArrayList; // 引入Java集合库中的ArrayList类，用于存储消息列表
import java.util.List; // 引入Java集合库中的List接口
import java.util.Map; // 引入Java集合库中的Map接口
import java.util.concurrent.ConcurrentHashMap; // 引入Java集合库中的ConcurrentHashMap类，用于线程安全的哈希表

public class Server {
    private static final int PORT = 12345; // 定义服务器端口号
    private OrderManager orderManager; // 订单管理器实例
    private Map<String, ClientHandler> clientHandlers; // 存储客户端处理器的映射表
    private Map<String, List<String>> clientMessages; // 存储每个客户端的消息队列
    private JTextArea messageArea; // 用于显示接收到的消息的文本区域
    private JComboBox<String> clientSelector; // 用于选择客户端的下拉列表

    // 构造函数，初始化订单管理器和映射表
    public Server(OrderManager orderManager) {
        this.orderManager = orderManager;
        this.clientHandlers = new ConcurrentHashMap<>();
        this.clientMessages = new ConcurrentHashMap<>();
    }

    // 启动服务器的方法
    public void start() {
        // 创建并设置JFrame
        JFrame frame = new JFrame("Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置关闭操作
        frame.setSize(400, 400); // 设置窗口大小

        // 创建并设置JPanel
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1)); // 设置网格布局，3行1列

        // 客户端选择器
        clientSelector = new JComboBox<>();
        clientSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedClient = (String) clientSelector.getSelectedItem(); // 获取选定的客户端ID
                if (selectedClient != null) {
                    showMessagesForClient(selectedClient); // 显示选定客户端的消息
                }
            }
        });
        panel.add(clientSelector); // 将客户端选择器添加到面板

        // 消息输入字段
        JTextField messageField = new JTextField();
        panel.add(messageField); // 将消息输入字段添加到面板

        // 发送消息按钮
        JButton sendButton = new JButton("Send Message to Selected Client");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedClient = (String) clientSelector.getSelectedItem(); // 获取选定的客户端ID
                String message = messageField.getText(); // 获取输入的消息
                if (selectedClient != null && !message.isEmpty()) {
                    sendMessageToClient(selectedClient, message); // 发送消息到选定客户端
                }
            }
        });
        panel.add(sendButton); // 将发送按钮添加到面板

        // 添加组件到JFrame
        frame.getContentPane().add(panel, BorderLayout.NORTH);

        // 消息显示区域
        messageArea = new JTextArea();
        messageArea.setEditable(false); // 设置消息区域为不可编辑
        messageArea.setLineWrap(true); // 设置自动换行
        messageArea.setWrapStyleWord(true); // 设置单词换行
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER); // 将消息区域添加到窗口中心

        frame.setVisible(true); // 显示窗口

        // 启动服务器Socket
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // 接受客户端连接
                String clientId = clientSocket.getRemoteSocketAddress().toString(); // 获取客户端ID
                ClientHandler clientHandler = new ClientHandler(clientSocket, orderManager, clientId); // 创建客户端处理器
                clientHandlers.put(clientId, clientHandler); // 存储客户端处理器
                clientMessages.put(clientId, new ArrayList<>()); // 初始化消息队列
                SwingUtilities.invokeLater(() -> clientSelector.addItem(clientId)); // 更新客户端选择器
                new Thread(clientHandler).start(); // 启动客户端处理线程
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 发送消息到指定客户端
    private void sendMessageToClient(String clientId, String message) {
        ClientHandler clientHandler = clientHandlers.get(clientId); // 获取客户端处理器
        if (clientHandler != null) {
            clientHandler.sendMessage(message); // 发送消息
        }
    }

    // 显示指定客户端的消息
    private void showMessagesForClient(String clientId) {
        List<String> messages = clientMessages.get(clientId); // 获取消息队列
        messageArea.setText(""); // 清空消息区域
        for (String message : messages) {
            messageArea.append("Client (" + clientId + "): " + message + "\n"); // 显示消息
        }
        messages.clear(); // 清空该客户端的消息队列
    }

    // 客户端处理器类
    class ClientHandler implements Runnable {
        private Socket clientSocket; // 客户端套接字
        private OrderManager orderManager; // 订单管理器
        private ObjectOutputStream oos; // 对象输出流
        private ObjectInputStream ois; // 对象输入流
        private String clientId; // 客户端ID

        // 构造函数，初始化客户端处理器
        public ClientHandler(Socket clientSocket, OrderManager orderManager, String clientId) {
            this.clientSocket = clientSocket;
            this.orderManager = orderManager;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try {
                oos = new ObjectOutputStream(clientSocket.getOutputStream()); // 初始化对象输出流
                ois = new ObjectInputStream(clientSocket.getInputStream()); // 初始化对象输入流

                while (true) {
                    String command = (String) ois.readObject(); // 接收命令
                    switch (command) {
                        case "add":
                            LogisticsOrder newOrder = (LogisticsOrder) ois.readObject(); // 接收新订单
                            orderManager.addOrder(newOrder); // 添加订单
                            oos.writeObject("Order added"); // 发送确认消息
                            break;
                        case "update":
                            String orderIDToUpdate = (String) ois.readObject(); // 接收要更新的订单ID
                            boolean delivered = (Boolean) ois.readObject(); // 接收订单状态
                            orderManager.updateOrder(orderIDToUpdate, delivered); // 更新订单
                            oos.writeObject("Order updated"); // 发送确认消息
                            break;
                        case "remove":
                            String orderIDToRemove = (String) ois.readObject(); // 接收要删除的订单ID
                            orderManager.removeOrder(orderIDToRemove); // 删除订单
                            oos.writeObject("Order removed"); // 发送确认消息
                            break;
                        case "query":
                            String orderIDToQuery = (String) ois.readObject(); // 接收要查询的订单ID
                            LogisticsOrder order = orderManager.getOrderById(orderIDToQuery); // 查询订单
                            oos.writeObject(order); // 发送订单信息
                            break;
                        case "message":
                            String message = (String) ois.readObject(); // 接收消息
                            System.out.println("Received message from client: " + message);
                            SwingUtilities.invokeLater(() -> {
                                clientMessages.get(clientId).add(message); // 添加消息到队列
                                if (clientId.equals(clientSelector.getSelectedItem())) {
                                    messageArea.append("Client (" + clientId + "): " + message + "\n"); // 显示消息
                                }
                            });
                            oos.writeObject("Message received: " + message); // 发送确认消息
                            break;
                        default:
                            oos.writeObject("Unknown command"); // 未知命令
                    }
                    oos.flush(); // 刷新输出流
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close(); // 关闭客户端连接
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clientHandlers.remove(clientId); // 移除客户端处理器
                clientMessages.remove(clientId); // 移除消息队列
                SwingUtilities.invokeLater(() -> clientSelector.removeItem(clientId)); // 更新客户端选择器
            }
        }

        // 发送消息到客户端
        public void sendMessage(String message) {
            try {
                oos.writeObject("message"); // 发送消息命令
                oos.writeObject(message); // 发送消息内容
                oos.flush(); // 刷新输出流
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 主函数，程序入口
    public static void main(String[] args) {
        OrderManager orderManager = new OrderManager(); // 创建订单管理器实例
        Server server = new Server(orderManager); // 创建服务器实例
        server.start(); // 启动服务器
    }
}
