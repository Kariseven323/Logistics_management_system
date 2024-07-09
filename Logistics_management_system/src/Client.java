import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private JFrame frame;
    private JTextField orderIDField;
    private JTextArea resultArea;

    public Client() {
        frame = new JFrame("Order Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));

        orderIDField = new JTextField();
        panel.add(new JLabel("Order ID:"));
        panel.add(orderIDField);

        JButton queryButton = new JButton("Query Order");
        queryButton.addActionListener(new QueryButtonListener());
        panel.add(queryButton);

        resultArea = new JTextArea();
        resultArea.setEditable(false);

        frame.getContentPane().add(panel, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(resultArea), BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private class QueryButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String orderID = orderIDField.getText();
            if (orderID.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Order ID cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                oos.writeObject("query");
                oos.writeObject(orderID);

                Object response = ois.readObject();
                if (response instanceof LogisticsOrder) {
                    LogisticsOrder order = (LogisticsOrder) response;
                    resultArea.setText(order.toString());
                } else {
                    resultArea.setText((String) response);
                }

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error connecting to server", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}
