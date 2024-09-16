import javax.swing.*;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;

public class Peer {
    private ServerSocket serverSocket;
    private Connection connection;
    private int loggedInUserId = -1;
    static final String DATABASE_URL = "jdbc:mysql://localhost/file_transfers";

    public Peer() {
        try {
            connection = DriverManager.getConnection(DATABASE_URL, "root", "!3wQ@f7Z#nX4");
            try (Statement stmt = connection.createStatement()) {
                String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                        "username VARCHAR(50) UNIQUE NOT NULL, " +
                        "password VARCHAR(255) NOT NULL)";
                stmt.execute(sqlUsers);

                String sqlTransfers = "CREATE TABLE IF NOT EXISTS transfers (" +
                        "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                        "file_name TEXT NOT NULL, " +
                        "file_size INTEGER NOT NULL, " +
                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "user_id INTEGER NOT NULL, " +
                        "direction ENUM('sent', 'received') NOT NULL, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id))";
                stmt.execute(sqlTransfers);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startClient(String serverAddress, int port, File fileToSend) {
        new Thread(() -> {
            try (Socket clientSocket = new Socket(serverAddress, port)) {
                System.out.println("Connected to server: " + serverAddress);
                handleServer(clientSocket, fileToSend);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleClient(Socket socket) {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(fileName));
            int returnValue = fileChooser.showSaveDialog(null);
            if (returnValue != JFileChooser.APPROVE_OPTION) {
                socket.close();
                return;
            }

            File fileToSave = fileChooser.getSelectedFile();
            try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesRead = 0;

                while ((bytesRead = dis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    if (totalBytesRead == fileSize) break;
                }
            }

            System.out.println("File received successfully.");
            logTransfer(fileName, fileSize, "received");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleServer(Socket socket, File fileToSend) {
        try (DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
             FileInputStream fileInputStream = new FileInputStream(fileToSend)) {

            dataOutputStream.writeUTF(fileToSend.getName());
            dataOutputStream.writeLong(fileToSend.length());

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("File sent to server successfully.");
            logTransfer(fileToSend.getName(), fileToSend.length(), "sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logTransfer(String fileName, long fileSize, String direction) {
        if (loggedInUserId == -1) return;

        String sql = "INSERT INTO transfers (file_name, file_size, user_id, direction) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, fileName);
            pstmt.setLong(2, fileSize);
            pstmt.setInt(3, loggedInUserId);
            pstmt.setString(4, direction);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadLogs(JTextArea logArea) {
        if (loggedInUserId == -1) return;

        String sql = "SELECT * FROM transfers WHERE user_id = ? ORDER BY timestamp DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, loggedInUserId);
            ResultSet rs = pstmt.executeQuery();
            StringBuilder logContent = new StringBuilder();
            while (rs.next()) {
                logContent.append("ID: ").append(rs.getInt("id"))
                        .append(", File: ").append(rs.getString("file_name"))
                        .append(", Size: ").append(rs.getLong("file_size"))
                        .append(", Direction: ").append(rs.getString("direction"))
                        .append(", Timestamp: ").append(rs.getString("timestamp"))
                        .append("\n");
            }
            SwingUtilities.invokeLater(() -> logArea.setText(logContent.toString()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean registerUser(String username, String password) {
        String hashedPassword = hashPassword(password);
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loginUser(String username, String password) {
        String hashedPassword = hashPassword(password);
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                loggedInUserId = rs.getInt("id");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
