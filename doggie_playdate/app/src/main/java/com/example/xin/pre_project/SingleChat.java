package com.example.xin.pre_project;

public class SingleChat{
    private String id;
    private String lastMessage;
    private String name;
    private String timestamp;
    private boolean seen;

    public SingleChat() {
    }

    public SingleChat(String lastMessage, String name, String timestamp, boolean seen) {
        this.lastMessage = lastMessage;
        this.name = name;
        this.timestamp = timestamp;
        this.seen = seen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLastMessage(String text) {
        this.lastMessage = text;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }
}
