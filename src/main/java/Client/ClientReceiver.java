package Client;

import Shared.Message;

import java.io.ObjectInputStream;

/**
 * Listens for messages from the server and displays them.
 */
public class ClientReceiver implements Runnable {
    private ObjectInputStream in;

    public ClientReceiver(ObjectInputStream in) {
        this.in = in;
    }

    public void run() {
        try {
            Message message;
            while ((message = (Message) in.readObject()) != null) {
                System.out.println("[" + message.getSender() + "]: " + message.getContent());
            }
        } catch (Exception e) {
            System.out.println("Disconnected from server.");
        }
    }
}
