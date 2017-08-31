package com.troopar.trooparapp.model;

import java.io.Serializable;

/**
 * Created by Barry on 12/04/2016.
 */
public class UserProfile implements Serializable{

    private int id;
    private String followNum;
    private String followerNum;
    private String postedReviewNum;
    private String postedPhotoNum;
    private String joinedEventNum;
    private String createdEventNum;
    private String followState;

    public UserProfile(String followerNum, String followNum, int id, String joinedEventNum, String postedPhotoNum, String postedReviewNum,String createdEventNum) {
        this.followerNum = followerNum;
        this.followNum = followNum;
        this.id = id;
        this.joinedEventNum = joinedEventNum;
        this.postedPhotoNum = postedPhotoNum;
        this.postedReviewNum = postedReviewNum;
        this.createdEventNum = createdEventNum;
    }

    public String getFollowerNum() {
        return followerNum;
    }

    public String getFollowNum() {
        return followNum;
    }

    public int getId() {
        return id;
    }

    public String getJoinedEventNum() {
        return joinedEventNum;
    }

    public String getPostedPhotoNum() {
        return postedPhotoNum;
    }

    public String getPostedReviewNum() {
        return postedReviewNum;
    }

    public String getCreatedEventNum() {
        return createdEventNum;
    }

    public String getFollowState() {
        return followState;
    }

    public void setFollowState(String followState) {
        this.followState = followState;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "createdEventNum='" + createdEventNum + '\'' +
                ", id=" + id +
                ", followNum='" + followNum + '\'' +
                ", followerNum='" + followerNum + '\'' +
                ", postedReviewNum='" + postedReviewNum + '\'' +
                ", postedPhotoNum='" + postedPhotoNum + '\'' +
                ", joinedEventNum='" + joinedEventNum + '\'' +
                '}';
    }


}
