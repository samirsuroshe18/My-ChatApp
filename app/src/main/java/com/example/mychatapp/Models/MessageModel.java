package com.example.mychatapp.Models;

public class MessageModel {
    String uId, message, messageId, groupMsgId, senderId;
    boolean seen;

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    long timestamp;

    public MessageModel(String uId, String message, long timestamp, boolean isSeen) {
        this.uId = uId;
        this.message = message;
        this.timestamp = timestamp;
        this.seen = isSeen;
    }

    public MessageModel(String uId, String message) {
        this.uId = uId;
        this.message = message;
    }

    public MessageModel(){}

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getGroupMsgId() {
        return groupMsgId;
    }

    public void setGroupMsgId(String groupMsgId) {
        this.groupMsgId = groupMsgId;
    }
}
