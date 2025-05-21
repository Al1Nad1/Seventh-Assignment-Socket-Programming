package Shared;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a message exchanged between client and server.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type; // e.g., "chat", "file", "login", "logout"
    private String sender;
    private String content;
    private LocalDateTime timestamp;

    public Message(String type, String sender, String content, LocalDateTime timestamp) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
