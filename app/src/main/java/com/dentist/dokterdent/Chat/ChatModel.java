package com.dentist.dokterdent.Chat;

public class ChatModel {
    private String userID;
    private String userName;
    private String photoName;
    private String unreadCount;
    private String lastMessage;
    private String lastMessageTime;

    public ChatModel(){

    }

    public ChatModel(String userID, String userName, String photoName) {
        this.userID = userID;
        this.userName = userName;
        this.photoName = photoName;
    }

    public ChatModel(String userID, String userName, String photoName, String unreadCount, String lastMessage, String lastMessageTime) {
        this.userID = userID;
        this.userName = userName;
        this.photoName = photoName;
        this.unreadCount = unreadCount;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    public String getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(String unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}
