package com.example.mychatapp.Models;

public class ChatItem {
    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_DATE = 1;
    private final int type;
    private MessageModel messageModel;
    private String dateHeader;

    // Constructor for message
    public ChatItem(MessageModel messageModel) {
        this.type = TYPE_MESSAGE;
        this.messageModel = messageModel;
    }

    // Constructor for date header
    public ChatItem(String dateHeader) {
        this.type = TYPE_DATE;
        this.dateHeader = dateHeader;
    }

    public int getType() { return type; }
    public MessageModel getMessageModel() { return messageModel; }
    public String getDateHeader() { return dateHeader; }
}