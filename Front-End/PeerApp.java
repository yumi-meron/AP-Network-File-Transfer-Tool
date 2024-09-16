import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PeerApp {

    private static File fileToSend;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame loginFrame = new JFrame("Login/Register");
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            loginFrame.setSize(400, 300);
            loginFrame.setLayout(new GridLayout(3, 2));

            JLabel userLabel = new JLabel("Username:");
            JTextField userField = new JTextField();
            JLabel passLabel = new JLabel("Password:");
            JPasswordField passField = new JPasswordField();

            JButton loginButton = new JButton("Login");
            JButton registerButton = new JButton("Register");

            loginFrame.add(userLabel);
            loginFrame.add(userField);
            loginFrame.add(passLabel);
            loginFrame.add(passField);
            loginFrame.add(loginButton);
            loginFrame.add(registerButton);

            loginFrame.setVisible(true);

            Peer peer = new Peer();

            loginButton.addActionListener(e -> {
                String username = userField.getText();
                String password = new String(passField.getPassword());
                if (peer.loginUser(username, password)) {
                    loginFrame.dispose();
                    showMainFrame(peer);
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Login Failed", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            registerButton.addActionListener(e -> {
                String username = userField.getText();
                String password = new String(passField.getPassword());
                if (peer.registerUser(username, password)) {
                    JOptionPane.showMessageDialog(loginFrame, "Registration Successful", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Registration Failed", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        });
    }

    private static void showMainFrame(Peer peer) {
        JFrame frame = new JFrame("P2P File Transfer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(2, 2));

        JLabel portLabel = new JLabel("Port:");
        JTextField portField = new JTextField("12345");
        topPanel.add(portLabel);
        topPanel.add(portField);

        JLabel serverAddressLabel = new JLabel("Server Address:");
        JTextField serverAddressField = new JTextField("localhost");
        topPanel.add(serverAddressLabel);
        topPanel.add(serverAddressField);

        frame.add(topPanel, BorderLayout.NORTH);

        JTextArea logArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(logArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(1, 3));

        JButton jbChooseFile = new JButton("Choose File");
        jbChooseFile.setFont(new Font("Arial", Font.BOLD, 20));

        JButton jbSendFile = new JButton("Send File");
        jbSendFile.setFont(new Font("Arial", Font.BOLD, 20));

        JButton loadLogsButton = new JButton("Load Transfer Logs");
        loadLogsButton.setFont(new Font("Arial", Font.BOLD, 20));

        bottomPanel.add(jbChooseFile);
        bottomPanel.add(jbSendFile);
        bottomPanel.add(loadLogsButton);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        jbChooseFile.addActionListener(e -> {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setDialogTitle("Choose a file to send.");

            if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                fileToSend = jFileChooser.getSelectedFile();
            }
        });

        jbSendFile.addActionListener(e -> {
            if (fileToSend == null) {
                JOptionPane.showMessageDialog(frame, "Please choose a file first.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                ExecutorService executor = Executors.newFixedThreadPool(1);
                executor.execute(() -> peer.startClient(serverAddressField.getText(), Integer.parseInt(portField.getText()), fileToSend));
            }
        });

        loadLogsButton.addActionListener(e -> peer.loadLogs(logArea));

        frame.setVisible(true);

        // Start server in a separate thread
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(() -> peer.startServer(Integer.parseInt(portField.getText())));
    }
}
