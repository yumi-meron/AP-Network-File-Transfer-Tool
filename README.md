# Peer-to-Peer (P2P) File Transfer Application

## Overview
This Java-based Peer-to-Peer (P2P) file transfer application allows users to send and receive files over a network with a simple graphical user interface (GUI) built using Swing. It includes user authentication, secure password hashing, and logs file transfers in a MySQL database.

## Features
- **User Authentication**: Secure registration and login with password hashing using SHA-256.
- **File Transfer**: Send and receive files between peers over the network.
- **Transfer Logs**: Automatically log file transfer history including file name, size, direction (sent/received), and timestamp.
- **GUI Interface**: Easy-to-use graphical interface built with Swing.

## Technologies Used
- **Java**: Core programming language.
- **Swing**: GUI framework for building the application interface.
- **MySQL**: Relational database for storing user credentials and file transfer logs.
- **JDBC**: Java Database Connectivity API for interacting with MySQL.
- **SHA-256**: Secure hashing algorithm for password security.
- **Socket Programming**: For network communication between peers.

## Getting Started

### Prerequisites
- **Java Development Kit (JDK)**
- **MySQL Server**
- **MySQL JDBC Driver** (Ensure it is included in your project classpath)

### Database Setup
1. Start your MySQL server.
2. Create a database named `file_transfers`:
   ```sql
   CREATE DATABASE file_transfers;
3. The application will automatically create the necessary tables (`users` and `transfers`) when it connects to the database.

### Configuration
Update the database connection string in the Peer class to match your MySQL server's configuration:
```
static final String DATABASE_URL = "jdbc:mysql://localhost/file_transfers";
```
Also, update the username and password in the DriverManager.getConnection() call:\
``` 
connection = DriverManager.getConnection(DATABASE_URL, "your_mysql_username", "your_mysql_password");
```
### Running the Application
1. Compile and run the PeerApp class to start the application.
2. A login/register window will appear. Register a new user or log in with existing credentials.
3. After logging in, the main application window will appear, allowing you to:
- Choose a file to send.
- Send the file to a peer by specifying the server address and port.
- Load transfer logs to view your transfer history.
4. The application can act as both a client and a server, facilitating P2P communication.

### User Guide
- Login/Register: Create a new account or log in with an existing account using the login/register window.
- Choose File: Select a file from your system to send using the "Choose File" button.
- Send File: Initiate the transfer by clicking "Send File" and specifying the server address and port.
- Load Logs: View your transfer history by clicking "Load Transfer Logs".

### Contributing
1. Fork the repository.
2. Create your feature branch: git checkout -b feature/YourFeature.
3. Commit your changes: git commit -m 'Add your feature'.
4. Push to the branch: git push origin feature/YourFeature.
5. Open a pull request.

### Acknowledgments
- Oracle for the Java Development Kit (JDK).
- MySQL for the database management system.
- Swing for the GUI framework.


