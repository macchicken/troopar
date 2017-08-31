package com.troopar.trooparapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import com.troopar.trooparapp.utils.Constants;

/**
 * Created by Barry on 7/01/2016.
 */
public class EventModel implements Serializable {

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;
    private String name;
    private String description;
    private String startDate;
    private String endDate;
    private String location;
    private int distance;
    private int totalLikes;

    public String getContact() {
        return contact;
    }

    private String contact;

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    private String longitude;
    private String latitude;

    public String getImage() {
        return image;
    }

    private String image;

    public String getCategory() {
        return category;
    }

    private String category;
    private String smallImageUrl;
    private String thumbnailImageUrl;
    private String mediumImageUrl;
    private String type= Constants.EVENT;
    private int maxJoinedPeople;
    private int minJoinedPeople;
    private int joinedProgress;
    private boolean joined;
    private int reviewNum;
    private String colorHex;
    private User user;
    private String createdDate;
    private String abbreviation;
    private ArrayList<User> joiners;
    private String requires;
    private String requireType;
    private int shareNum;


    public int getDistance() {
        return distance;
    }

    public EventModel(){}

    public EventModel(String id, String name, String description, String startDate, String endDate, String location,
                      String image, String category, String longitude, String latitude, String contact,int distance,int reviewNum) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.image = image;
        this.category = category;
        this.longitude = longitude;
        this.latitude = latitude;
        this.contact = contact;
        this.distance=distance;
        this.reviewNum=reviewNum;
        this.joiners=new ArrayList<>();
    }

    public EventModel(String id, String name, String description, String startDate, String endDate, String location,String image, String category, String longitude, String latitude, String contact
                      ,int distance,int reviewNum,int maxJoinedPeople,int minJoinedPeople,int joinedProgress,boolean joined,String colorHex,User user,String createdDate) {
        this(id, name, description, startDate, endDate, location, image, category, longitude, latitude, contact, distance, reviewNum);
        this.maxJoinedPeople=maxJoinedPeople;
        this.minJoinedPeople=minJoinedPeople;
        this.joinedProgress=joinedProgress;
        this.joined=joined;
        this.colorHex="#"+colorHex;
        this.user=user;
        this.createdDate=createdDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getLocation() {
        return location;
    }

    public String getColorHex() {
        return colorHex;
    }

    public User getUser() {
        return user;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = "#"+colorHex;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventModel that = (EventModel) o;
        return name.equals(that.name);

    }

    @Override
    public String toString() {
        return "EventModel{" +
                "category='" + category + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", location='" + location + '\'' +
                ", distance=" + distance +
                ", contact='" + contact + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", image='" + image + '\'' +
                '}';
    }

    public String getSmallImageUrl() {
        return smallImageUrl;
    }

    public void setSmallImageUrl(String smallImageUrl) {
        this.smallImageUrl = smallImageUrl;
    }

    public String getThumbnailImageUrl() {
        return thumbnailImageUrl;
    }

    public void setThumbnailImageUrl(String thumbnailImageUrl) {
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    public int getReviewNum() {
        return reviewNum;
    }

    public int getMaxJoinedPeople() {
        return maxJoinedPeople;
    }

    public int getMinJoinedPeople() {
        return minJoinedPeople;
    }

    public int getJoinedProgress() {
        return joinedProgress;
    }

    public String getType() {
        return type;
    }

    public boolean isJoined() {
        return joined;
    }

    public int getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(int totalLikes) {
        this.totalLikes = totalLikes;
    }

    public boolean addJoiners(User user){
        return joiners.add(user);
    }

    public ArrayList<User> getJoiners() {
        return joiners;
    }

    public void resetJoiners(int total) {
        this.joiners = new ArrayList<>(total);
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }

    public String getRequires() {
        return requires;
    }

    public void setRequires(String requires) {
        this.requires = requires;
    }

    public String getRequireType() {
        return requireType;
    }

    public void setRequireType(String requireType) {
        this.requireType = requireType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMediumImageUrl() {
        return mediumImageUrl;
    }

    public void setMediumImageUrl(String mediumImageUrl) {
        this.mediumImageUrl = mediumImageUrl;
    }

    public int getShareNum() {
        return shareNum;
    }

    public void setShareNum(int shareNum) {
        this.shareNum = shareNum;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }


}
