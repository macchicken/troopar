package com.troopar.trooparapp.model;


import java.io.Serializable;


/**
 * Created by Barry on 28/01/2016.
 */
public class UploadPhoto implements Serializable {

    private String id;
    private String createdDate;
    private String photoPath;
    private String smallImagePath;
    private String description;
    private String totalLikes;
    private boolean liked;


    public UploadPhoto(String id,String createdDate, String description, String photoPath, String smallImagePath, String totalLikes) {
        this.id = id;
        this.createdDate = createdDate;
        this.description = description;
        this.photoPath = photoPath;
        this.smallImagePath = smallImagePath;
        this.totalLikes = totalLikes;
    }

    public String getTotalLikes() {
        return totalLikes;
    }

    public String getSmallImagePath() {
        return smallImagePath;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    @Override
    public String toString() {
        return "UploadPhoto{" +
                "createdDate='" + createdDate + '\'' +
                ", id='" + id + '\'' +
                ", photoPath='" + photoPath + '\'' +
                ", smallImagePath='" + smallImagePath + '\'' +
                ", description='" + description + '\'' +
                ", totalLikes='" + totalLikes + '\'' +
                '}';
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
