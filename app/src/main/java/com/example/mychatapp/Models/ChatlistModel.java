package com.example.mychatapp.Models;
public class ChatlistModel {

    String profilepic;
    String userName;
    String userId;
    String lastMessage;
    String lastMessageBy;
    boolean typing;

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    boolean isOnline;

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }

    public String getLastMessageBy() {
        return lastMessageBy;
    }

    public void setLastMessageBy(String lastMessageBy) {
        this.lastMessageBy = lastMessageBy;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    String about;
    boolean isRead, isReadByUser;
    String fcmToken;

    public boolean isReadByUser() {
        return isReadByUser;
    }

    public void setReadByUser(boolean readByUser) {
        isReadByUser = readByUser;
    }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public long getReadCount() {
        return unreadCount;
    }

    public void setReadCount(long readCount) {
        this.unreadCount = readCount;
    }

    long lastMsgTime, unreadCount=0;

    public long getLastMsgTime() {
        return lastMsgTime;
    }

    public void setLastMsgTime(long lastMsgTime) {
        this.lastMsgTime = lastMsgTime;
    }

    public ChatlistModel(String profilepic, String userName, String mail, String password, String userId, String lastMessage, long lastMsgTime, boolean isRead, long unreadCount) {
        this.profilepic = profilepic;
        this.userName = userName;
        this.userId = userId;
        this.lastMessage = lastMessage;
        this.lastMsgTime = lastMsgTime;
        this.isRead = isRead;
        this.unreadCount = unreadCount;
    }

    public ChatlistModel(){}

    //Sign up constructor

    public ChatlistModel(String userName, String mail, String password) {
        this.userName = userName;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}