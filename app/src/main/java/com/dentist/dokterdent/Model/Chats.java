package com.dentist.dokterdent.Model;

public class Chats implements Comparable<Chats>{

    private String id,name,photo,lastMessage;
    private Long lastMessageTime;
    private int unreadCount;

    public Chats() {
    }

    public Chats(String id,Long lastMessageTime) {
        this.id = id;
        this.lastMessageTime = lastMessageTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    @Override
    public int compareTo(Chats chat) {
        if (lastMessageTime == chat.getLastMessageTime()) {
            return 0;
        }else if(lastMessageTime > chat.getLastMessageTime()){
            return 1;
        }else{
            return -1;
        }
    }
}


