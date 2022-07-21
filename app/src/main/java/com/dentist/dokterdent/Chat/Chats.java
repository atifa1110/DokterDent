package com.dentist.dokterdent.Chat;

public class Chats {
    private String user_id;
    private String user_name;
    private String photo_name;
    private String unread_count;
    private String last_message;
    private String last_message_time;

    public Chats() {
    }

    public Chats(String user_id, String user_name, String photo_name, String unread_count, String last_message, String last_message_time) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.photo_name = photo_name;
        this.unread_count = unread_count;
        this.last_message = last_message;
        this.last_message_time = last_message_time;
    }

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String user_id) {
        this.user_id = user_id;
    }

    public String getUserName() {
        return user_name;
    }

    public void setUserName(String user_name) {
        this.user_name = user_name;
    }

    public String getPhotoName() {
        return photo_name;
    }

    public void setPhotoName(String photo_name) {
        this.photo_name = photo_name;
    }

    public String getUnreadCount() {
        return unread_count;
    }

    public void setUnreadCount(String unread_count) {
        this.unread_count = unread_count;
    }

    public String getLastMessage() {
        return last_message;
    }

    public void setLastMessage(String last_message) {
        this.last_message = last_message;
    }

    public String getLastMessageTime() {
        return last_message_time;
    }

    public void setLastMessageTime(String last_message_time) {
        this.last_message_time = last_message_time;
    }
}


