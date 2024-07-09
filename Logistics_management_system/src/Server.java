import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;

    public Server(int port) throws Exception {
        serverSocket = new ServerSocket(port);
    }

    public void start(OrderManager orderManager) {
        System.out.println("Server started...");
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket, orderManager).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            OrderManager orderManager = new OrderManager();
            Server server = new Server(12345);
            server.start(orderManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    private Socket clientSocket;
    private OrderManager orderManager;

    public ClientHandler(Socket clientSocket, OrderManager orderManager) {
        this.clientSocket = clientSocket;
        this.orderManager = orderManager;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String command = in.readLine();
            String orderID = in.readLine();

            switch (command) {
                case "add":
                    LogisticsOrder newOrder = new LogisticsOrder(orderID, "Customer " + orderID, "Address " + orderID, new java.util.Date(), Math.random() * 1000, false);
                    orderManager.addOrder(newOrder);
                    out.println("Order added: " + orderID);
                    break;
                case "update":
                    orderManager.updateOrder(orderID, true);
                    out.println("Order updated: " + orderID);
                    break;
                case "remove":
                    orderManager.removeOrder(orderID);
                    out.println("Order removed: " + orderID);
                    break;
                default:
                    out.println("Unknown command");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
