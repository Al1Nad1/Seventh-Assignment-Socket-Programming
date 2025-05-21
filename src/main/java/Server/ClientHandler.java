package Server;

import Shared.Message;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Handles communication with a single client.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private List<ClientHandler> clients;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;

    public ClientHandler(Socket socket, List<ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
    }

    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Read the username
            Message loginMessage = (Message) in.readObject();
            this.username = loginMessage.getSender();
            broadcast(new Message("chat", "Server", username + " has joined the chat.", null));

            Message message;
            while ((message = (Message) in.readObject()) != null) {
                if ("chat".equalsIgnoreCase(message.getType())) {
                    broadcast(message);
                } else if ("logout".equalsIgnoreCase(message.getType())) {
                    broadcast(new Message("chat", "Server", username + " has left the chat.", null));
                    break;
                }
                // Handle other message types like file transfer here
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Connection with client " + username + " lost.");
        } finally {
            try {
                clients.remove(this);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcast(Message message) {
        for (ClientHandler client : clients) {
            try {
                client.out.writeObject(message);
                client.out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
