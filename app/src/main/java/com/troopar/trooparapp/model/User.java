package com.troopar.trooparapp.model;


import java.io.Serializable;

/**
 * Created by Barry on 21/01/2016.
 */
public class User implements Serializable{

    private int id;
    private String userName;
    private String firstName;
    private String lastName;
    private String gender;
    private String phone;
    private String email;
    private String avatarOrigin;
    private String avatarStandard;
    private String remark;
    private UserProfile userProfile;
    private int unreadMessageCount;


    public User(String firstName, String gender, int id, String lastName, String userName,String avatarOrigin,String avatarStandard) {
        this.firstName = firstName;
        this.gender = gender;
        this.id = id;
        this.lastName = lastName;
        this.userName = userName;
        this.avatarOrigin = avatarOrigin;
        this.avatarStandard = avatarStandard;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatarOrigin() {
        return avatarOrigin;
    }

    public String getAvatarStandard() {
        return avatarStandard;
    }

    @Override
    public String toString() {
        return "User{" +
                "avatarOrigin='" + avatarOrigin + '\'' +
                ", id=" + id +
                ", userName='" + userName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender='" + gender + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", avatarStandard='" + avatarStandard + '\'' +
                '}';
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(int unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public boolean equals(Object o) {
        if (o!=null&&o instanceof User){
            User ot=(User) o;
            if (this.id==-5||this.id==-3){return this.firstName.equals(ot.getFirstName());}
            return this.id==ot.getId();
        }else{
            return super.equals(o);
        }
    }


}
