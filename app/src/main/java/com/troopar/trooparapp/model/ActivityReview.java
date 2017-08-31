package com.troopar.trooparapp.model;

import java.io.Serializable;

/**
 * Created by Barry on 5/08/2016.
 * review of an activity
 */
public class ActivityReview implements Serializable{

    private int id;
    private int aId;
    private User user;
    private int respondToUserId;
    private String content;
    private String createdDate;
    private String type;
    private boolean liked;

    public ActivityReview() {
    }

    public ActivityReview(String type) {
        this.type = type;
    }

    public ActivityReview(int aId, String content, String createdDate, int id, int respondToUserId, User user,String type) {
        this.aId = aId;
        this.content = content;
        this.createdDate = createdDate;
        this.id = id;
        this.respondToUserId = respondToUserId;
        this.user = user;
        this.type = type;
    }

    public int getaId() {
        return aId;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public int getId() {
        return id;
    }

    public int getRespondToUserId() {
        return respondToUserId;
    }

    public User getUser() {
        return user;
    }

    public String getType() {
        return type;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

}
