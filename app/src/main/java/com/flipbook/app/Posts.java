package com.flipbook.app;

import java.util.ArrayList;

/**
 * Created by Hayden on 2017-03-06.
 */

public class Posts {

    private String username, caption, likes;

    private int speed;
    private ArrayList<String> imageUrlArray;

    public Posts(String username, String caption, String likes, int speed, ArrayList<String> imageUrlArray){
        this.setUsername(username);
        this.setCaption(caption);
        this.setLikes(likes);
        this.setImages(imageUrlArray);
        this.setSpeed(speed);
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ArrayList<String> getImages() {
        return imageUrlArray;
    }

    public void setImages(ArrayList<String> imageUrlArray) {
        this.imageUrlArray = imageUrlArray;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
