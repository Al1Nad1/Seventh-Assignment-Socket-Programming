package Client;

import Shared.Message;
import java.io.ObjectInputStream;

public class ClientReceiver implements Runnable {
    private final ObjectInputStream in;

    public ClientReceiver(ObjectInputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof Message message) {
                    System.out.println(message.getSender() + ": " + message.getContent());
                }
            }
        } catch (Exception e) {
            System.out.println("Disconnected from chat.");
        }
    }
}