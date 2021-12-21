package com.dentist.dokterdent.Chat;

public class GroupModel {
    String groupId,groupTitle,groupIcon,timestamp;

    public GroupModel(){

    }

    public GroupModel(String groupId, String groupTitle, String groupIcon, String timestamp) {
        this.groupId = groupId;
        this.groupTitle = groupTitle;
        this.groupIcon = groupIcon;
        this.timestamp = timestamp;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    public String getGroupIcon() {
        return groupIcon;
    }

    public void setGroupIcon(String groupIcon) {
        this.groupIcon = groupIcon;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
