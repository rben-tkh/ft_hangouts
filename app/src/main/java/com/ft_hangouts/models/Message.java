package com.ft_hangouts.models;

public class Message {
    private int id;
    private final String messageText;
    private final boolean isSent;
    private final long timestamp;

    public Message(int id, String messageText, boolean isSent, long timestamp) {
        this.id = id;
        this.messageText = messageText;
        this.isSent = isSent;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessageText() {
        return messageText;
    }

    public boolean isSent() {
        return isSent;
    }

    public long getTimestamp() {
        return timestamp;
    }
}