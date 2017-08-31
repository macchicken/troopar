package com.troopar.trooparapp.model;

import java.io.Serializable;

/**
 * Created by Barry on 24/02/2016.
 */
public class MessageModel implements Serializable {

    private String from;
    private String to;
    private String createdTime;
    private String content;
    private String id;
    private int rowId;
    private String eventId;
    private boolean isComMeg=true;
    private String type="chat";
    private boolean grouped=false;
    private String groupUsers;


    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getEventId() {
        return eventId;
    }

    public MessageModel(String id,String createdTime, String content) {
        this.id=id;
        this.createdTime = createdTime;
        this.content = content;
    }

    public MessageModel(String content, String createdTime, String id, String from,String to,String eventId,boolean isComMeg) {
        this(id,createdTime,content);
        this.from = from;
        this.to = to;
        this.eventId=eventId;
        this.isComMeg=isComMeg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageModel that = (MessageModel) o;
        return content.equals(that.content);

    }

    public boolean getMsgType() {
        return isComMeg;
    }

    public void setMsgType(boolean isComMsg) {
        isComMeg = isComMsg;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }

    public boolean isGrouped() {
        return grouped;
    }

    public void setGrouped(boolean grouped) {
        this.grouped = grouped;
    }

    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public String getGroupUsers() {
        return groupUsers;
    }

    public void setGroupUsers(String groupUsers) {
        this.groupUsers = groupUsers;
    }


}
