package Server;

import Shared.Message;
import Shared.Message.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private List<ClientHandler> allClients;
    private String username;

    public ClientHandler(Socket socket, List<ClientHandler> allClients) throws IOException {
        this.socket = socket;
        this.allClients = allClients;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof Message) {
                    Message msg = (Message) obj;

                    switch (msg.getType()) {
                        case LOGIN:
                            handleLogin(msg);
                            break;
                        case CHAT:
                            broadcastChat(msg);
                            break;
                        case UPLOAD:
                            receiveFile(msg);
                            break;
                        case DOWNLOAD:
                            sendFile(msg);
                            break;
                        case FILE_LIST:
                            sendFileList();
                            break;
                        default:
                            sendMessage(new Message("Server", "Unknown message type", MessageType.SYSTEM));
                            break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Client disconnected: " + username);
        } finally {
            allClients.remove(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastChat(Message msg) {
        for (ClientHandler client : allClients) {
            if (client != this) {
                client.sendMessage(new Message(username, msg.getContent(), MessageType.CHAT));
            }
        }
    }

    private void handleLogin(Message msg) {
        String[] parts = msg.getContent().split(":", 2);
        if (parts.length < 2) {
            sendMessage(new Message("Server", "LOGIN_FAILED", MessageType.SYSTEM));
            return;
        }

        String username = parts[0];
        String password = parts[1];

        if (Server.authenticate(username, password)) {
            this.username = username;
            sendMessage(new Message("Server", "LOGIN_SUCCESS", MessageType.SYSTEM));
        } else {
            sendMessage(new Message("Server", "LOGIN_FAILED", MessageType.SYSTEM));
        }
    }

    private void sendFileList() {
        File dir = new File("resources");
        if (!dir.exists()) dir.mkdir();

        String[] files = dir.list();
        String list = (files != null) ? String.join(",", files) : "";
        sendMessage(new Message("Server", list, MessageType.FILE_LIST));
    }

    private void sendFile(Message msg) {
        try {
            File file = new File("resources/" + msg.getContent());
            if (!file.exists()) {
                sendMessage(new Message("Server", "ERROR: File not found", MessageType.SYSTEM));
                return;
            }

            byte[] buffer = new FileInputStream(file).readAllBytes();
            sendMessage(new Message("Server", msg.getContent(), MessageType.DOWNLOAD)); // Notify file transfer
            out.writeObject(buffer); // Send file content
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveFile(Message msg) {
        try {
            Object obj = in.readObject();
            if (!(obj instanceof byte[])) {
                sendMessage(new Message("Server", "UPLOAD_FAILED: Invalid file data", MessageType.UPLOAD));
                return;
            }

            byte[] data = (byte[]) obj;

            File dir = new File("resources");
            if (!dir.exists()) dir.mkdir();

            try (FileOutputStream fos = new FileOutputStream("resources/" + msg.getContent())) {
                fos.write(data);
            }

            sendMessage(new Message("Server", "UPLOAD_SUCCESS:" + msg.getContent(), MessageType.UPLOAD));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            sendMessage(new Message("Server", "UPLOAD_FAILED:" + msg.getContent(), MessageType.UPLOAD));
        }
    }
}