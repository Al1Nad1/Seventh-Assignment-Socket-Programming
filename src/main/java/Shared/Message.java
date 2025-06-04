package Shared;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sender;
    private String content;
    private MessageType type;

    public enum MessageType {
        LOGIN,
        CHAT,
        UPLOAD,
        DOWNLOAD,
        FILE_LIST,
        SYSTEM
    }

    public Message(String sender, String content, MessageType type) {
        this.sender = sender;
        this.content = content;
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }
}