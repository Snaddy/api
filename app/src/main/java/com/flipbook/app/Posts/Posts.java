package com.flipbook.app.Posts;

import java.util.ArrayList;

/**
 * Created by Hayden on 2017-03-06.
 */

public class Posts {


    private String username;
    private String caption;
    private String id;
    private String userId;

    private String postDate;
    private boolean isLiked;
    private ArrayList<String> images;
    private int speed;
    private int likes_count;

    public Posts(String username, String caption, String id ,int likes_count, int speed, ArrayList<String> images, boolean isLiked, String userId, String postDate){
        this.setUsername(username);
        this.setCaption(caption);
        this.id = id;
        this.setLikesCount(likes_count);
        this.setImages(images);
        this.setSpeed(speed);
        this.isLiked = isLiked;
        this.userId = userId;
        this.postDate = postDate;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public int getLikesCount() {
        return likes_count;
    }

    public void setLikesCount(int likes_count) {
        this.likes_count = likes_count;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getSpeed(){
        return speed;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Boolean getLiked(){
        return isLiked;
    }
    public void setLiked(Boolean isLiked){
        this.isLiked = isLiked;
    }

    public void setLikedByUser(boolean isLiked){
        this.isLiked = isLiked;
    }

    public String getPostDate() {
        return postDate;
    }
}