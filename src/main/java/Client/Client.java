package Client;

import Shared.Message;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * The main client class that connects to the server.
 */
public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5050;

    public static void main(String[] args) {
        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Scanner scanner = new Scanner(System.in)
        ) {
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();

            // Send login message
            out.writeObject(new Message("login", username, "", LocalDateTime.now()));
            out.flush();

            // Start a thread to listen for messages from the server
            new Thread(new ClientReceiver(in)).start();

            // Read messages from the user and send to the server
            String messageText;
            while (true) {
                messageText = scanner.nextLine();
                if ("/exit".equalsIgnoreCase(messageText)) {
                    out.writeObject(new Message("logout", username, "", LocalDateTime.now()));
                    out.flush();
                    break;
                } else {
                    out.writeObject(new Message("chat", username, messageText, LocalDateTime.now()));
                    out.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
3