import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.Date;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private JFrame frame;
    private JTextField orderIDField;
    private JTextField senderField;
    private JTextField receiverField;
    private JTextField itemField;
    private JCheckBox deliveredCheckBox;
    private JTextArea resultArea;
    private JTextField messageField;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public Client() {
        frame = new JFrame("Order Client");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(500, 400);

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
                            socket.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    frame.dispose();
                    System.exit(0);
                }
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2, 10, 10));

        panel.add(new JLabel("Order ID:"));
        orderIDField = new JTextField();
        panel.add(orderIDField);

        panel.add(new JLabel("Customer Name:"));
        senderField = new JTextField();
        panel.add(senderField);

        panel.add(new JLabel("Address:"));
        receiverField = new JTextField();
        panel.add(receiverField);

        panel.add(new JLabel("Money:"));
        itemField = new JTextField();
        panel.add(itemField);

        panel.add(new JLabel("Delivered:"));
        deliveredCheckBox = new JCheckBox();
        panel.add(deliveredCheckBox);

        JButton addButton = new JButton("Add Order");
        addButton.addActionListener(new AddButtonListener());
        panel.add(addButton);

        JButton updateButton = new JButton("Update Order");
        updateButton.addActionListener(new UpdateButtonListener());
        panel.add(updateButton);

        JButton removeButton = new JButton("Remove Order");
        removeButton.addActionListener(new RemoveButtonListener());
        panel.add(removeButton);

        JButton queryButton = new JButton("Query Order");
        queryButton.addActionListener(new QueryButtonListener());
        panel.add(queryButton);

        panel.add(new JLabel("Message:"));
        messageField = new JTextField();
        panel.add(messageField);

        JButton sendMessageButton = new JButton("Send Message");
        sendMessageButton.addActionListener(new SendMessageButtonListener());
        panel.add(sendMessageButton);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);

        frame.getContentPane().add(panel, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(resultArea), BorderLayout.CENTER);

        frame.setVisible(true);

        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            new Thread(new ServerMessageListener()).start();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error connecting to server", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class ServerMessageListener implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    String command = (String) ois.readObject();
                    if ("message".equals(command)) {
                        String message = (String) ois.readObject();
                        resultArea.append("Server: " + message + "\n");
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private class SendMessageButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = messageField.getText();

            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Message field must be filled", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                oos.writeObject("message");
                oos.writeObject(message);
                oos.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error sending message to server", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private class AddButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String orderID = orderIDField.getText();
            String customerName = senderField.getText();
            String address = receiverField.getText();
            String money = itemField.getText();
            boolean delivered = deliveredCheckBox.isSelected();

            if (orderID.isEmpty() || address.isEmpty() || customerName.isEmpty() || money.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields must be filled", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double mon;
            try {
                mon = Double.parseDouble(money);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Item must be a valid number", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Date orderDate = new Date();
            LogisticsOrder newOrder = new LogisticsOrder(orderID, customerName, address, orderDate, mon, delivered);

            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                oos.writeObject("add");
                oos.writeObject(newOrder);

                String response = (String) ois.readObject();
                resultArea.setText(response);

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error connecting to server", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class UpdateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String orderID = orderIDField.getText();
            boolean delivered = deliveredCheckBox.isSelected();

            if (orderID.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Order ID must be filled", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                oos.writeObject("update");
                oos.writeObject(orderID);
                oos.writeObject(delivered);

                String response = (String) ois.readObject();
                resultArea.setText(response);

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error connecting to server", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class RemoveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String orderID = orderIDField.getText();

            if (orderID.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Order ID must be filled", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                oos.writeObject("remove");
                oos.writeObject(orderID);

                String response = (String) ois.readObject();
                resultArea.setText(response);

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error connecting to server", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class QueryButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String orderID = orderIDField.getText();

            if (orderID.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Order ID must be filled", "Error", JOptionPane.ERROR_MESSAGE);
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
