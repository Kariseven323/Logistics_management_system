import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public Client(String ip, int port) throws Exception {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void sendCommand(String command, String orderID) throws Exception {
        out.println(command);
        out.println(orderID);
        System.out.println("Server response: " + in.readLine());
    }

    public void closeConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            Client client = new Client("127.0.0.1", 12345);
            client.sendCommand("add", "101");
            client.sendCommand("update", "101");
            client.sendCommand("remove", "101");
            client.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
