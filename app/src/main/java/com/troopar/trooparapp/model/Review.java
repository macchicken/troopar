package com.troopar.trooparapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Barry on 15/01/2016.
 */
public class Review implements Serializable{

    private String userName;
    private String reviewTime;
    private String content;
    private String rating;
    private List<Comment> comments;
    private String userImage;
    private String id;

    public Review(String userName, String reviewTime, String content,String rating,String userImage) {
        this.userName = userName;
        this.content = content;
        this.reviewTime = reviewTime;
        this.rating = rating;
        this.userImage = userImage;
        this.comments=new ArrayList<>();
    }

    public String getContent() {
        return content;
    }

    public String getReviewTime() {
        return reviewTime;
    }

    public String getUserName() {
        return userName;
    }

    public String getRating() {
        return rating;
    }

    public void addCommnet(Comment comment){
        this.comments.add(comment);
    }

    public List<Comment> getCommnets(){
        return comments;
    }

    public String getUserImage() {
        return userImage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Review{" +
                "content='" + content + '\'' +
                ", rating='" + rating + '\'' +
                ", reviewTime='" + reviewTime + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }


}
