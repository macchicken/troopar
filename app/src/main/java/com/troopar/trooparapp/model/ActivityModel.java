package com.troopar.trooparapp.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Barry on 28/01/2016.
 */
public class ActivityModel implements Serializable{

    private String userName;
    private String id;
    private String type;
    private EventModel event;
    private Review review;
    private List<UploadPhoto> uploadPhotos;
    private String createdTime;
    private int totalLikes;
    private int shareNum;
    private int reviewNum;


    public ActivityModel(String type) {
        this.type=type;
    }

    public ActivityModel(String userName,String id, String type,EventModel event, Review review, List<UploadPhoto> uploadPhotos) {
        this.userName=userName;
        this.id = id;
        this.type=type;
        this.event = event;
        this.review = review;
        this.uploadPhotos = uploadPhotos;
    }

    public EventModel getEvent() {
        return event;
    }

    public String getId() {
        return id;
    }

    public Review getReview() {
        return review;
    }

    public String getType() {
        return type;
    }

    public List<UploadPhoto> getUploadPhotos() {
        return uploadPhotos;
    }

    public String getUserName() {
        return userName;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public int getReviewNum() {
        return reviewNum;
    }

    public void setReviewNum(int reviewNum) {
        this.reviewNum = reviewNum;
    }

    public int getShareNum() {
        return shareNum;
    }

    public void setShareNum(int shareNum) {
        this.shareNum = shareNum;
    }

    public int getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(int totalLikes) {
        this.totalLikes = totalLikes;
    }

    @Override
    public String toString() {
        return "ActivityModel{" +
                "event=" + event +
                ", id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", review=" + review +
                ", uploadPhoto=" + uploadPhotos +
                '}';
    }


}
