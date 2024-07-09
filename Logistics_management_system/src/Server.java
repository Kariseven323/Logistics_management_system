import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final int PORT = 12345;
    private OrderManager orderManager;
    private Map<String, ClientHandler> clientHandlers;
    private JTextArea messageArea;  // 用于显示接收到的消息
    private JComboBox<String> clientSelector;  // 用于选择客户端

    public Server(OrderManager orderManager) {
        this.orderManager = orderManager;
        this.clientHandlers = new ConcurrentHashMap<>();
    }

    public void start() {
        JFrame frame = new JFrame("Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));

        clientSelector = new JComboBox<>();
        panel.add(clientSelector);

        JTextField messageField = new JTextField();
        panel.add(messageField);

        JButton sendButton = new JButton("Send Message to Selected Client");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedClient = (String) clientSelector.getSelectedItem();
                String message = messageField.getText();
                if (selectedClient != null && !message.isEmpty()) {
                    sendMessageToClient(selectedClient, message);
                }
            }
        });
        panel.add(sendButton);

        frame.getContentPane().add(panel, BorderLayout.NORTH);

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);

        frame.setVisible(true);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientId = clientSocket.getRemoteSocketAddress().toString();
                ClientHandler clientHandler = new ClientHandler(clientSocket, orderManager, clientId);
                clientHandlers.put(clientId, clientHandler);
                SwingUtilities.invokeLater(() -> clientSelector.addItem(clientId));
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToClient(String clientId, String message) {
        ClientHandler clientHandler = clientHandlers.get(clientId);
        if (clientHandler != null) {
            clientHandler.sendMessage(message);
        }
    }

    class ClientHandler implements Runnable {
        private Socket clientSocket;
        private OrderManager orderManager;
        private ObjectOutputStream oos;
        private ObjectInputStream ois;
        private String clientId;

        public ClientHandler(Socket clientSocket, OrderManager orderManager, String clientId) {
            this.clientSocket = clientSocket;
            this.orderManager = orderManager;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try {
                oos = new ObjectOutputStream(clientSocket.getOutputStream());
                ois = new ObjectInputStream(clientSocket.getInputStream());

                while (true) {
                    String command = (String) ois.readObject();
                    switch (command) {
                        case "add":
                            LogisticsOrder newOrder = (LogisticsOrder) ois.readObject();
                            orderManager.addOrder(newOrder);
                            oos.writeObject("Order added");
                            break;
                        case "update":
                            String orderIDToUpdate = (String) ois.readObject();
                            boolean delivered = (Boolean) ois.readObject();
                            orderManager.updateOrder(orderIDToUpdate, delivered);
                            oos.writeObject("Order updated");
                            break;
                        case "remove":
                            String orderIDToRemove = (String) ois.readObject();
                            orderManager.removeOrder(orderIDToRemove);
                            oos.writeObject("Order removed");
                            break;
                        case "query":
                            String orderIDToQuery = (String) ois.readObject();
                            LogisticsOrder order = orderManager.getOrderById(orderIDToQuery);
                            oos.writeObject(order);
                            break;
                        case "message":
                            String message = (String) ois.readObject();
                            System.out.println("Received message from client: " + message);
                            SwingUtilities.invokeLater(() -> {
                                if (clientId.equals(clientSelector.getSelectedItem())) {
                                    messageArea.append("Client (" + clientId + "): " + message + "\n");
                                }
                            });
                            oos.writeObject("Message received: " + message);
                            break;
                        default:
                            oos.writeObject("Unknown command");
                    }
                    oos.flush();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clientHandlers.remove(clientId);
                SwingUtilities.invokeLater(() -> clientSelector.removeItem(clientId));
            }
        }

        public void sendMessage(String message) {
            try {
                oos.writeObject("message");
                oos.writeObject(message);
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        OrderManager orderManager = new OrderManager();
        Server server = new Server(orderManager);
        server.start();
    }
}
