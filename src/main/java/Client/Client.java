package Client;

import Shared.Message;
import Shared.Message.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static String username;

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345)) {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            Scanner scanner = new Scanner(System.in);
            System.out.println("===== Welcome to CS Music Room =====");

            boolean loggedIn = false;
            while (!loggedIn) {
                System.out.print("Username: ");
                username = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();

                sendLoginRequest(username, password);

                Object responseObj = in.readObject();
                if (responseObj instanceof Message responseMsg) {
                    if (responseMsg.getType() == MessageType.SYSTEM && "LOGIN_SUCCESS".equals(responseMsg.getContent())) {
                        loggedIn = true;
                        System.out.println("Login successful!");
                    } else {
                        System.out.println("Login failed. Try again.");
                    }
                } else {
                    System.out.println("Invalid response from server.");
                }
            }

            while (true) {
                printMenu();
                System.out.print("Enter choice: ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1" -> enterChat(scanner);
                    case "2" -> uploadFile(scanner);
                    case "3" -> requestDownload(scanner);
                    case "0" -> {
                        System.out.println("Exiting...");
                        out.writeObject(new Message(username, "EXIT", MessageType.SYSTEM));
                        out.flush();
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Enter chat box");
        System.out.println("2. Upload a file");
        System.out.println("3. Download a file");
        System.out.println("0. Exit");
    }

    private static void sendLoginRequest(String username, String password) throws IOException {
        Message loginMsg = new Message(username, password, MessageType.LOGIN);
        out.writeObject(loginMsg);
        out.flush();
    }

    private static void enterChat(Scanner scanner) throws IOException, ClassNotFoundException {
        System.out.println("You have entered the chat. Type /exit to leave.");

        // Start a thread to listen to incoming chat messages from server
        new Thread(new ClientReceiver(in)).start();

        String message;
        while (!(message = scanner.nextLine()).equalsIgnoreCase("/exit")) {
            sendChatMessage(message);
        }
        // Send exit chat message to server
        out.writeObject(new Message(username, "EXIT_CHAT", MessageType.SYSTEM));
        out.flush();
    }

    private static void sendChatMessage(String messageToSend) throws IOException {
        Message msg = new Message(username, messageToSend, MessageType.CHAT);
        out.writeObject(msg);
        out.flush();
    }

    private static void uploadFile(Scanner scanner) throws IOException {
        File dir = new File("resources/Client/" + username);
        File[] files = dir.listFiles();

        if (files == null || files.length == 0) {
            System.out.println("No files to upload.");
            return;
        }

        System.out.println("Select a file to upload:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }

        System.out.print("Enter file number: ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (choice < 0 || choice >= files.length) {
            System.out.println("Invalid choice.");
            return;
        }

        File fileToUpload = files[choice];
        byte[] fileBytes = new byte[(int) fileToUpload.length()];
        try (FileInputStream fis = new FileInputStream(fileToUpload)) {
            fis.read(fileBytes);
        }

        // First send upload message with filename
        out.writeObject(new Message(username, fileToUpload.getName(), MessageType.UPLOAD));
        out.flush();
        // Then send actual file bytes
        out.writeObject(fileBytes);
        out.flush();

        System.out.println("File uploaded successfully.");
    }

    private static void requestDownload(Scanner scanner) throws IOException, ClassNotFoundException {
        // Request file list
        out.writeObject(new Message(username, "", MessageType.FILE_LIST));
        out.flush();

        Object response = in.readObject();
        if (!(response instanceof Message msgResponse) || msgResponse.getType() != MessageType.FILE_LIST) {
            System.out.println("Invalid server response.");
            return;
        }

        String listString = msgResponse.getContent();
        String[] files = listString.isEmpty() ? new String[0] : listString.split(",");

        if (files.length == 0) {
            System.out.println("No files available for download.");
            return;
        }

        System.out.println("Available files:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i]);
        }

        System.out.print("Enter file number to download: ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (choice < 0 || choice >= files.length) {
            System.out.println("Invalid choice.");
            return;
        }

        String fileName = files[choice];
        // Send download request
        out.writeObject(new Message(username, fileName, MessageType.DOWNLOAD));
        out.flush();

        // Wait for server confirmation message for download
        Object serverMsgObj = in.readObject();
        if (!(serverMsgObj instanceof Message serverMsg) || serverMsg.getType() != MessageType.DOWNLOAD) {
            System.out.println("Unexpected server response.");
            return;
        }

        // Then receive file bytes
        Object fileDataObj = in.readObject();
        if (!(fileDataObj instanceof byte[] fileBytes)) {
            System.out.println("Invalid file data received.");
            return;
        }

        File outputDir = new File("resources/Client/" + username);
        if (!outputDir.exists()) outputDir.mkdirs();

        File downloadedFile = new File(outputDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(downloadedFile)) {
            fos.write(fileBytes);
        }

        System.out.println("File downloaded to: " + downloadedFile.getPath());
    }
}